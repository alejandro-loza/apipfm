package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.services.ResumeService

import javax.inject.Inject

@Controller("/resume")
@Validated
@Secured('isAuthenticated()')
class ResumeController {

    @Inject
    ResumeService resumeService

    @Get("/expenses/account/{accountId}")
    @Transactional
    List<TransactionDto> expenses(Long accountId) {
        List<Transaction> response = resumeService.getExpenses(accountId)
        response.collect{new TransactionDto(it)}
    }

}
