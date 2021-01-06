package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.ResumeService
import mx.finerio.pfm.api.services.UserService

import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand

import javax.inject.Inject

class MovementsAnalisisServiceImp {
    public static final boolean EXPENSE = true

    @Inject
    UserService userService

    @Inject
    AccountService accountService

    @Inject
    ResumeService resumeService

    def getAnalisis(MovementAnalysisFilterParamsCommand cmd){
        Date fromDate = cmd.dateFrom ? resumeService.validateFromDate(cmd.dateFrom) : resumeService.getFromLimit()
        Date toDate = cmd.dateTo ? resumeService.validateToDate(cmd.dateTo , fromDate) : new Date()
        List<Account> userAccounts = accountService.findAllByUser(userService.getUser(cmd.userId))
        List<MovementsDto> expensesResult = resumeService.getExpensesResume(userAccounts, fromDate, toDate )

    }



}
