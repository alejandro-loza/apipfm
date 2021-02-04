package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import mx.finerio.pfm.api.dtos.utilities.ResumeDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.ResumeService
import mx.finerio.pfm.api.validation.ResumeFilterParamsCommand

import javax.annotation.Nullable
import javax.inject.Inject

@Controller("/resume")
@Validated
@Secured('isAuthenticated()')
class  ResumeController {

    @Inject
    ResumeService resumeService

    @Log
    @Get("{?accountId,dateFrom,dateTo}")
    @Transactional
    ResumeDto resume(
            @QueryValue('userId') Long userId,
            @Nullable Long accountId,
            @Nullable Long dateFrom,
            @Nullable Long dateTo) {

        resumeService.getResume(userId, new ResumeFilterParamsCommand(accountId, dateFrom, dateTo))
    }

}
