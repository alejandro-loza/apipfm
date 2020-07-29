package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.utilities.BalancesDto
import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import mx.finerio.pfm.api.dtos.utilities.ResumeDto
import mx.finerio.pfm.api.logging.Log

interface ResumeService {

    @Log
    List<Transaction> getExpenses(Long accountId)

    @Log
    List<Transaction> getIncomes(Long userId)

    @Log
    List<MovementsDto> getTransactionsGroupByMonth(List<Transaction> transactionList)

    @Log
    ResumeDto getResume(Long userId)

    @Log
    List<BalancesDto>  getBalance(List<MovementsDto> incomesResult, List<MovementsDto> expensesResult)

}
