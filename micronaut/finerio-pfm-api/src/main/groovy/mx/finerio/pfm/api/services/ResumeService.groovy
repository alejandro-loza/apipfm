package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.utilities.BalancesDto
import mx.finerio.pfm.api.dtos.utilities.MovementsAnalysisDto
import mx.finerio.pfm.api.dtos.utilities.MovementsResumeDto
import mx.finerio.pfm.api.dtos.utilities.ResumeDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.ResumeFilterParamsCommand

interface ResumeService {

    @Log
    List<MovementsResumeDto> resumeTransactionsGroupByMonth(List<Transaction> transactionList)

    @Log
    List<MovementsAnalysisDto> analysisTransactionsGroupByMonth(List<Transaction> transactionList)

    @Log
    ResumeDto getResume(Long userId, ResumeFilterParamsCommand cmd)

    @Log
    List<BalancesDto>  getBalance(List<MovementsResumeDto> incomesResult, List<MovementsResumeDto> expensesResult)

    @Log
    List<MovementsResumeDto> getExpensesResume(List<Account> accounts, Date fromDate, Date toDate)

    @Log
    Date getFromLimit()

    @Log
    Date validateFromDate(Long dateFrom)

    @Log
    Date validateToDate(Long dateTo, Date from)
    
}
