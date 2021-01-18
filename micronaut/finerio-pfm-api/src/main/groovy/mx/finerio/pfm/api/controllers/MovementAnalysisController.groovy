package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import mx.finerio.pfm.api.dtos.utilities.MovementsResponseDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.MovementsAnalysisService
import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand

import javax.annotation.Nullable
import javax.inject.Inject

@Controller("/analysis")
@Validated
@Secured('isAuthenticated()')
class MovementAnalysisController {

    @Inject
    MovementsAnalysisService movementAnalysisService

    @Log
    @Get("{?dateFrom,dateTo}")
    @Transactional
    MovementsResponseDto resume(
            @QueryValue('userId') Long userId,
            @Nullable Long dateFrom,
            @Nullable Long dateTo) {
      new MovementsResponseDto(
              movementAnalysisService.getAnalysis(userId, new MovementAnalysisFilterParamsCommand( dateFrom, dateTo))
      )
    }

}
