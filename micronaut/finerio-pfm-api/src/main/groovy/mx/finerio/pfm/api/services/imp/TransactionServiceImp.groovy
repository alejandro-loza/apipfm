package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject

class TransactionServiceImp  implements TransactionService {

    public static final int MAX_ROWS = 100

    @Inject
    TransactionGormService transactionGormService

    @Inject
    AccountService accountService

    @Inject
    CategoryService categoryService

    @Override
    @Transactional
    Transaction create(TransactionCreateCommand cmd){
        verifyBody(cmd)
        Transaction transaction = new Transaction()
        transaction.with {
            account =  accountService.getAccount(cmd.accountId)
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

        transactionGormService.save(transaction)
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
                .collect{new TransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByCursor(Long cursor) {
        transactionGormService
                .findAllByDateDeletedIsNullAndIdLessThanEquals(
                        cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new TransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByAccountAndCursor(Account account, Long cursor) {
        transactionGormService
                .findAllByAccountAndIdLessThanEqualsAndDateDeletedIsNull(
                        account, cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new TransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByAccount(Account account) {
        transactionGormService
                .findAllByAccountAndDateDeletedIsNull(account, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new TransactionDto(it)}
    }

    @Override
    List<Transaction> findAllByAccountAndChargeAndDateRange(Account account, Boolean charge, Date from, Date to) {
        transactionGormService
                .findAllByAccountAndChargeAndDateDeletedIsNullAndDateBetween(
                        account, charge, from, to, [ sort: 'id', order: 'desc'])
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
}
