package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.utilities.MovementsAnalysisDto
import mx.finerio.pfm.api.services.*
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
    List<MovementsAnalysisDto>  getAnalysis(Long userId, MovementAnalysisFilterParamsCommand cmd){
        Date fromDate = cmd.dateFrom ? resumeService.validateFromDate(cmd.dateFrom) : resumeService.getFromLimit()
        Date toDate = cmd.dateTo ? resumeService.validateToDate(cmd.dateTo , fromDate) : new Date()
        List<Transaction> transactions = transactionService.getAccountsTransactions(
                accountService.findAllByUser(userService.getUser(userId)),
                EXPENSE, fromDate, toDate)

        resumeService.analysisTransactionsGroupByMonth(transactions)
    }

}
