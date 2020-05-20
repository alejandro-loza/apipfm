package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.validation.AccountCommand
import mx.finerio.pfm.api.validation.TransactionCommand

import javax.inject.Inject

class TransactionServiceImp implements TransactionService {

    public static final int MAX_ROWS = 100

    @Inject
    TransactionGormService transactionGormService

    @Inject
    AccountService accountService

    @Override
    Transaction create(TransactionCommand cmd){
        transactionGormService.save(new Transaction(cmd, accountService.getAccount(cmd.accountId)) )
    }

    @Override
    Transaction find(Long id) {
        Optional.ofNullable(transactionGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new NotFoundException('The account ID you requested was not found.') })
    }

    @Override
    Transaction update(TransactionCommand cmd, Long id){
        Transaction transaction = find(id)
        transaction.with {
            account = accountService.getAccount(cmd.accountId)
            date = new Date(cmd.date)
            description = cmd.description
            charge = cmd.charge
            amount = cmd.amount
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

}
