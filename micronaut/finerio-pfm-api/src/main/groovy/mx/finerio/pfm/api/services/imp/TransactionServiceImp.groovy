package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import mx.finerio.pfm.api.clients.CategorizerDeclarativeClient
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.services.gorm.SystemCategoryGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.validation.AccountUpdateCommand
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject

@ConfigurationProperties('categorizer')
@Requires(property = 'categorizer')
class TransactionServiceImp  implements TransactionService {

    public static final int MAX_ROWS = 100

    @Inject
    TransactionGormService transactionGormService

    @Inject
    AccountService accountService

    @Inject
    CategoryService categoryService

    @Inject
    SystemCategoryGormService systemCategoryGormService

    @Inject
    CategorizerDeclarativeClient categorizerDeclarativeClient

    private String username

    private String password

    @Override
    @Transactional
    Transaction create(TransactionCreateCommand cmd) {
        verifyBody(cmd)
        Account transactionAccount = accountService.getAccount(cmd.accountId)
        Transaction transaction = new Transaction()
        transaction.with {
            account = transactionAccount
            date =  new Date(cmd.date)
            description = cmd.description
            charge =  cmd.charge
            amount = cmd.amount
        }
        if(cmd.categoryId){
            Category category = categoryService.getById(cmd.categoryId)
            verifyParentCategory(category)
            transaction.category = category
        }
        else{
            transaction.systemCategory = systemCategoryGormService.findByFinerioConnectId(
                    categorizerDeclarativeClient.getCategories(getAuthorizationHeader(), cmd.description).categoryId
            )
        }
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
            date = cmd.date ? new Date(cmd.date) : new Date()
            description = cmd.description ?: transaction.description
            charge = cmd.charge != null? cmd.charge: transaction.charge
            amount = cmd.amount ?: transaction.amount
        }
        if(cmd.categoryId){
            Category category = categoryService.getById(cmd.categoryId)
            verifyParentCategory(category)
            transaction.category = category
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
    List<TransactionDto> findAllByAccountAndCursor(Account account, Long cursor) {
        transactionGormService
                .findAllByAccountAndIdLessThanEqualsAndDateDeletedIsNull(
                        account, cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{generateTransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByAccount(Account account) {
        transactionGormService
                .findAllByAccountAndDateDeletedIsNull(account, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{generateTransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByCategory(Category category) {
        transactionGormService.findAllByCategory(category)
                .collect{generateTransactionDto(it)}
    }

    @Override
    void deleteAllByAccount(Account account) {
        transactionGormService.deleteAllByAccount(account)
    }

    @Override
    List<Transaction> findAllByAccountAndChargeAndDateRange(Account account, Boolean charge, Date from, Date to) {
        transactionGormService
                .findAllByAccountAndChargeAndDateDeletedIsNullAndDateBetween(
                        account, charge, from, to, [ sort: 'id', order: 'desc'])
    }

    @Override
    TransactionDto generateTransactionDto(transaction) {
        TransactionDto transactionDto = new TransactionDto()
        transactionDto.with {
            id = transaction.id
            date = transaction.date
            charge = transaction.charge
            description = transaction.description
            amount = transaction.amount
            dateCreated = transaction.dateCreated
            lastUpdated = transaction.lastUpdated
            categoryId = transaction.category
                    ? transaction?.category?.id
                    : transaction?.systemCategory?.id
        }
        transactionDto
    }


    private static void verifyBody(ValidationCommand cmd) {
        if (!cmd) {
            throw new IllegalArgumentException(
                    'request.body.invalid')
        }
    }

    private static void verifyParentCategory(Category category) {
        if (!category?.parent) {
            throw new BadRequestException('category.parentCategory.null')
        }
    }

    private String getAuthorizationHeader()  throws Exception {
        def authEncoded = "${username}:${password}"
                .bytes.encodeBase64().toString()
         "Basic ${authEncoded}"
    }
}
