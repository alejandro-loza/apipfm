package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.*
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionFiltersCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand
import javax.inject.Inject

class TransactionServiceImp extends ServiceTemplate implements TransactionService {

    public static final int NO_SYSTEM_CATEGORY = 92
    @Inject
    TransactionGormService transactionGormService

    @Inject
    AccountService accountService

    @Inject
    CategoryService categoryService

    @Inject
    SystemCategoryService systemCategoryService

    @Inject
    CategorizerService categorizerService

    @Inject
    TransactionFilterService transactionFilterService

    @Override
    @Transactional
    @CompileStatic
    Transaction create(TransactionCreateCommand cmd) {
        verifyBody(cmd)
        Account transactionAccount = accountService.getAccount(cmd.accountId)
        Transaction transaction = new Transaction()
        transaction.with {
            account = transactionAccount
            executionDate =  new Date(cmd.date)
            description = cmd.description
            charge =  cmd.charge
            amount = cmd.amount as Float
        }
        setTransactionCategory(cmd, transaction)
        transactionGormService.save(transaction)

        if(transactionAccount.chargeable) {
            accountService.updateBalanceByTransaction(transaction)
        }
        return transaction
    }

    @Override
    @Transactional
    Transaction find(Long id) {
        Optional.ofNullable(transactionGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('transaction.notFound') })
    }

    @Override
    @Transactional
    Transaction update(TransactionUpdateCommand cmd, Long id){
        verifyBody(cmd)
        Transaction transaction = find(id)
        transaction.with {
            account = cmd.accountId ? accountService.getAccount(cmd.accountId) : transaction.account
            executionDate = cmd.date ? new Date(cmd.date) : new Date()
            description = cmd.description ?: transaction.description
            charge = cmd.charge != null? cmd.charge: transaction.charge
            amount = cmd.amount ?: transaction.amount
        }
        if(cmd.categoryId){
            setSystemCategoryOrCategory(cmd, transaction)
        }
        else{
            tryToSetSystemCategoryByCategorizer(cmd, transaction)
        }
        transactionGormService.save(transaction)
    }

    @Override
    void delete(Long id){
        Transaction transaction = find(id)
        transaction.dateDeleted = new Date()
        transactionGormService.save(transaction)
    }

    @Override
    List<TransactionDto> getAll() {
        transactionGormService
                .findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{generateTransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByCursor(Long cursor) {
        transactionGormService
                .findAllByDateDeletedIsNullAndIdLessThanEquals(
                        cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{generateTransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByAccountAndCursor(Account account, TransactionFiltersCommand cmd) {
        List<TransactionDto> transactions = transactionGormService
                .findAllByAccountAndIdLessThanEqualsAndDateDeletedIsNull(
                        account, cmd.cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{generateTransactionDto(it)}

        if(isTransactionFiltersCommandNonEmpty(cmd) && transactions){
            return transactionFilterService.filterTransactions(transactions, cmd)
        }

        transactions
    }

    @Override
    List<TransactionDto> findAllByAccountAndFilters(Account account, TransactionFiltersCommand cmd) {
        List<TransactionDto> transactionDtos = findAllByAccount(account)

        if(isTransactionFiltersCommandNonEmpty(cmd) && transactionDtos){
            return transactionFilterService.filterTransactions(transactionDtos, cmd)
        }
        transactionDtos
    }

    @Override
    List<TransactionDto> findAllByAccount(Account account) {
        transactionGormService
                .findAllByAccountAndDateDeletedIsNull(account, [max: MAX_ROWS, sort: 'id', order: 'desc'])//todo verify max rows
                .collect { generateTransactionDto(it) }
    }

    @Override
    List<TransactionDto> findAllByCategory(Category category) {
        transactionGormService.findAllByCategory(category)
                .collect{generateTransactionDto(it)}
    }

    @Override
    List<Transaction> findAllByCategoryChargeAndDateFrom(Category category, Date dateFrom, Boolean charge) {
        transactionGormService
                .findAllByCategoryAndExecutionDateGreaterThanEqualsAndChargeAndDateDeletedIsNull(category, dateFrom, charge)
    }

    @Override
    List<Transaction> findAllByAccountSystemCategoryChargeAndDateFrom(
            Account account, SystemCategory systemCategory, Date dateFrom, Boolean charge) {
        transactionGormService
                .getAllByAccountSystemCategoryExecutionDateGreaterCharge(
                        account, systemCategory, dateFrom, charge)
    }

    @Override
    void deleteAllByAccount(Account account) {
        transactionGormService.deleteAllByAccount(account)
    }

    @Override
    List<Transaction> findAllByAccountAndChargeAndDateRange(Account account, Boolean charge, Date from, Date to) {
        transactionGormService
                .findAllByAccountAndChargeAndDateDeletedIsNullAndExecutionDateBetween(
                        account, charge, from, to, [ sort: 'id', order: 'desc'])
    }

    @Override
    List<Transaction> getAccountsTransactions(List<Account> accounts, Boolean charge, Date dateFrom, Date dateTo) {
        List<Transaction> transactions = []
        for ( account in accounts ) {
            transactions.addAll(findAllByAccountAndChargeAndDateRange(account, charge, dateFrom, dateTo))
        }
        transactions

    }

    @Override
    TransactionDto generateTransactionDto(Transaction transaction) {
        TransactionDto transactionDto = new TransactionDto()
        transactionDto.with {
            id = transaction.id
            date = transaction.executionDate
            charge = transaction.charge
            description = transaction.description
            amount = new BigDecimal(transaction.amount).setScale(2,BigDecimal.ROUND_HALF_UP)
            dateCreated = transaction.dateCreated
            lastUpdated = transaction.lastUpdated
            categoryId = transaction.category
                    ? transaction?.category?.id
                    : transaction?.systemCategory?.id
        }
        transactionDto
    }

    private static void verifyParentCategory(Category category) {
        if (!category?.parent) {
            throw new BadRequestException('category.parentCategory.null')
        }
    }

    void tryToSetSystemCategoryByCategorizer(ValidationCommand cmd, Transaction transaction) {
        String cleanedDescription = categorizerService.cleanText(cmd["description"] as String,
            transaction.charge).result
        String categoryId = categorizerService.searchCategory(cleanedDescription,
            transaction.charge, false)?.categoryId
        if(categoryId){
            transaction.systemCategory = systemCategoryService.findByFinerioConnectId(categoryId)
            transaction.description = cleanedDescription
        }
    }

    void setSystemCategoryOrCategory(ValidationCommand cmd, Transaction transaction) {
        Long categoryId = cmd["categoryId"] as Long
        SystemCategory systemCategory = systemCategoryService.find(categoryId)
        if (systemCategory) {
            verifySystemParentCategory(systemCategory)
            transaction.systemCategory = systemCategory
        }
        else {
            Category category = categoryService.getById(categoryId)
            verifyParentCategory(category)
            transaction.category = category
        }
    }

    private static void verifySystemParentCategory(SystemCategory systemCategory) {
        if (!systemCategory?.parent) {
            throw new BadRequestException('category.parentCategory.null')
        }
    }

    private boolean isTransactionFiltersCommandNonEmpty(TransactionFiltersCommand cmd) {
        !transactionFilterService.generateProperties(cmd).isEmpty()
    }


    private void setTransactionCategory(TransactionCreateCommand cmd, Transaction transaction) {
        if (cmd.categoryId) {
            setSystemCategoryOrCategory(cmd, transaction)
        } else {
            tryToSetSystemCategoryByCategorizer(cmd, transaction)
        }
        if (!transaction.category && !transaction.systemCategory) {
            transaction.systemCategory = systemCategoryService.find(NO_SYSTEM_CATEGORY)
        }
    }

}
