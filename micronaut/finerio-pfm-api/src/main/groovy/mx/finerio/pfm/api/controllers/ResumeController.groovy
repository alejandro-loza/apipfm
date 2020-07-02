package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import javax.annotation.Nullable
import mx.finerio.pfm.api.dtos.ResumeDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.services.ResumeService

import javax.inject.Inject

@Controller("/resume")
@Validated
@Secured('isAuthenticated()')
class ResumeController {

    @Inject
    ResumeService resumeService

    @Get("/expenses/user/{userId}")
    @Transactional
    List<TransactionDto> expenses(Long userId) {
        resumeService.getExpenses(userId).collect{new TransactionDto(it)}
    }

    @Get("{?userId}")
    @Transactional
    ResumeDto resume(@Nullable Long userId) {
        resumeService.getResume(userId)
    }

}
