package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.MovementsDto
import mx.finerio.pfm.api.dtos.TransactionDto

interface ResumeService {
    List<TransactionDto> getExpenses(Long accountId)
    List<TransactionDto> getIncomes(Long userId)
    List<MovementsDto> getTransactionsGroupByMonth(List<Transaction> transactionList)

}