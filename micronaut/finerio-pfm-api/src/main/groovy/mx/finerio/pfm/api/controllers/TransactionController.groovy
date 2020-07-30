package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.NextCursorService
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller("/transactions")
@Validated
@Secured('isAuthenticated()')
class TransactionController {

    @Inject
    TransactionService transactionsService

    @Inject
    AccountService accountService

    @Inject
    NextCursorService nextCursorService

    @Log
    @Post("/")
    Single<TransactionDto> save(@Body @Valid TransactionCreateCommand cmd){
        Single.just(new TransactionDto(transactionsService.create(cmd)))
    }

    @Log
    @Get("/{id}")
    @Transactional
    Single<TransactionDto> show(@NotNull Long id) {
        Single.just(new TransactionDto(transactionsService.find(id)))
    }

    @Log
    @Get("{?cursor}")
    @Transactional
    Single<ResourcesDto> showAll(@Nullable Long cursor, @QueryValue('accountId') Long accountId) {
        Account account = accountService.getAccount(accountId)
        nextCursorService.generateResourcesDto( cursor ?
                transactionsService.findAllByAccountAndCursor(account, cursor)
                : transactionsService.findAllByAccount(account)
        )
    }

    @Log
    @Put("/{id}")
    @Transactional
    Single<TransactionDto> edit(@Body TransactionUpdateCommand cmd, @NotNull Long id ) {
        Single.just(new TransactionDto(transactionsService.update(cmd, id)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        transactionsService.delete(id)
        HttpResponse.noContent()
    }

}
