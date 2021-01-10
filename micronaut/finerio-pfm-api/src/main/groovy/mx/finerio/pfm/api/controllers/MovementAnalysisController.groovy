package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import mx.finerio.pfm.api.dtos.utilities.ResumeDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.MovementsAnalisisService
import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand
import mx.finerio.pfm.api.validation.ResumeFilterParamsCommand

import javax.annotation.Nullable
import javax.inject.Inject

@Controller("/movementAnalysis")
@Validated
@Secured('isAuthenticated()')
class MovementAnalysisController {

    @Inject
    MovementsAnalisisService movementAnalysisService

    @Log
    @Get("{?dateFrom,dateTo}")
    @Transactional
    List<MovementsDto> resume(
            @QueryValue('userId') Long userId,
            @Nullable Long dateFrom,
            @Nullable Long dateTo) {
      def response =  movementAnalysisService.getAnalysis(userId, new MovementAnalysisFilterParamsCommand( dateFrom, dateTo))
    response
    }

}
