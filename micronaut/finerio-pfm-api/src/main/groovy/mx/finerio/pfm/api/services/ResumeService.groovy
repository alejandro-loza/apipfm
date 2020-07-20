package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.BalancesDto
import mx.finerio.pfm.api.dtos.MovementsDto
import mx.finerio.pfm.api.dtos.ResumeDto

interface ResumeService {
    List<Transaction> getExpenses(Long accountId)
    List<Transaction> getIncomes(Long userId)
    List<MovementsDto> getTransactionsGroupByMonth(List<Transaction> transactionList)
    ResumeDto getResume(Long userId)
    List<BalancesDto>  getBalance(List<MovementsDto> incomesResult, List<MovementsDto> expensesResult)
}
