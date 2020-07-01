package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand

interface TransactionService {
    Transaction create(TransactionCreateCommand cmd)
    Transaction find(Long id)
    Transaction update(TransactionUpdateCommand cmd, Long id)
    void delete(Long id)
    List<TransactionDto> getAll()
    List<TransactionDto> findAllByCursor(Long cursor)
    List<TransactionDto> findAllByAccountAndCursor(Account account, Long cursor)
    List<TransactionDto> findAllByAccount(Account account)
    List<Transaction> findAllByAccountAndCharge(Account account, Boolean charge)
}