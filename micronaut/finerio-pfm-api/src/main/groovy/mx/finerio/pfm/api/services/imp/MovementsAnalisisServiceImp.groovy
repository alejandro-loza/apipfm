package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.MovementsAnalisisService
import mx.finerio.pfm.api.services.ResumeService
import mx.finerio.pfm.api.services.UserService

import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand

import javax.inject.Inject

class MovementsAnalisisServiceImp implements MovementsAnalisisService {

    @Inject
    UserService userService

    @Inject
    AccountService accountService

    @Inject
    ResumeService resumeService

    @Override
    @Transactional
    List<MovementsDto>  getAnalysis(Long userId, MovementAnalysisFilterParamsCommand cmd){
        Date fromDate = cmd.dateFrom ? resumeService.validateFromDate(cmd.dateFrom) : resumeService.getFromLimit()
        Date toDate = cmd.dateTo ? resumeService.validateToDate(cmd.dateTo , fromDate) : new Date()
        resumeService.getExpensesResume(
                accountService.findAllByUser(userService.getUser(userId)),
                fromDate, toDate )
    }

}
