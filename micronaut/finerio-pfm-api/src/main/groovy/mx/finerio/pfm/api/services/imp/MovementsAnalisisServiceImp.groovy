package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.utilities.MovementsResumeDto
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.MovementsAnalysisService
import mx.finerio.pfm.api.services.ResumeService
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.services.UserService

import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand

import javax.inject.Inject

class MovementsAnalisisServiceImp implements MovementsAnalysisService {
    public static final boolean EXPENSE = true

    @Inject
    UserService userService

    @Inject
    AccountService accountService

    @Inject
    ResumeService resumeService

    @Inject
    TransactionService transactionService

    @Override
    @Transactional
    List<MovementsResumeDto>  getAnalysis(Long userId, MovementAnalysisFilterParamsCommand cmd){
        Date fromDate = cmd.dateFrom ? resumeService.validateFromDate(cmd.dateFrom) : resumeService.getFromLimit()
        Date toDate = cmd.dateTo ? resumeService.validateToDate(cmd.dateTo , fromDate) : new Date()
        List<Transaction> transactions = transactionService.getAccountsTransactions(
                accountService.findAllByUser(userService.getUser(userId)),
                EXPENSE, fromDate, toDate)

        resumeService.analysisTransactionsGroupByMonth(transactions)
    }

}
