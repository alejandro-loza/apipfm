package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.MovementsDto

interface ResumeService {
    List<Transaction> getExpenses(Long accountId)
    List<Transaction> getIncomes(Long userId)
    List<MovementsDto> getTransactionsGroupByMonth(List<Transaction> transactionList)

}