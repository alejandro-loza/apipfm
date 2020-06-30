package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject
import java.time.ZonedDateTime

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
        Transaction transaction = new Transaction(cmd, accountService.getAccount(cmd.accountId))
        transaction.category = findCategory(cmd)
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
            account = cmd.accountId
                    ? accountService.getAccount(cmd.accountId)
                    : transaction.account
            date = cmd.date ? new Date(cmd.date) : new Date()
            description = cmd.description ?: transaction.description
            charge = cmd.charge != null? cmd.charge: transaction.charge
            amount = cmd.amount ?: transaction.amount
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
        transactionGormService.findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new TransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByCursor(Long cursor) {
        transactionGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new TransactionDto(it)}
    }

    @Override
    List<TransactionDto> findAllByAccountAndCursor(Account account, Long cursor) {
        transactionGormService.findAllByAccountAndIdLessThanEqualsAndDateDeletedIsNull(account, cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new TransactionDto(it)}
    }

    @Override
    List<Transaction> findAllByAccountAndCharge(Account account, Boolean charge) {
        transactionGormService
                .findAllByAccountAndDateGreaterThanAndChargeAndDateDeletedIsNull(
                        account,  dateSixMonthsAgo(),charge, [max: MAX_ROWS, sort: 'id', order: 'desc'])
    }

    private static Date dateSixMonthsAgo() {
        Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
    }

    private Category findCategory(TransactionCreateCommand cmd){
        if(!cmd.categoryId){
           return null
        }
         return categoryService.find(cmd.categoryId)
    }

    static void verifyBody(ValidationCommand cmd) {
        if (!cmd) {
            throw new IllegalArgumentException(
                    'request.body.invalid')
        }
    }
}
