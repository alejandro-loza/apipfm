package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.utilities.BalancesDto
import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import mx.finerio.pfm.api.dtos.utilities.ResumeDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.ResumeFilterParamsCommand

interface ResumeService {

    @Log
    List<MovementsDto> groupTransactionsByMonth(List<Transaction> transactionList)

    @Log
    ResumeDto getResume(Long userId, ResumeFilterParamsCommand cmd)

    @Log
    List<BalancesDto>  getBalance(List<MovementsDto> incomesResult, List<MovementsDto> expensesResult)

    @Log
    List<MovementsDto> getExpensesResume(List<Account> accounts, Date fromDate, Date toDate)

    @Log
    Date getFromLimit()

    @Log
    Date validateFromDate(Long dateFrom)

    @Log
    Date validateToDate(Long dateTo, Date from)
    
}
