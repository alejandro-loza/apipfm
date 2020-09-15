package mx.finerio.pfm.api.controllers


import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.resource.AccountDto

import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.NextCursorService
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.AccountUpdateCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller("/accounts")
@Validated
@Secured('isAuthenticated()')
class AccountController {

    @Inject
    AccountService accountService

    @Inject
    NextCursorService nextCursorService

    @Inject
    TransactionService transactionService

    @Log
    @Post("/")
    @Transactional
    Single<AccountDto> save(@Body @Valid AccountCreateCommand cmd){
        Single.just(new AccountDto(accountService.create(cmd)))
    }

    @Log
    @Get("/{id}")
    @Transactional
    Single<AccountDto> show(@NotNull Long id) {
        Single.just(new AccountDto(accountService.getAccount(id)))
    }

    @Log
    @Get("{?cursor}")
    @Transactional
    Single<ResourcesDto> showAll(@Nullable Long cursor, @QueryValue('userId') Long userId ) {
        nextCursorService.generateResourcesDto(cursor ?
                accountService.findAllByUserAndCursor(userId, cursor)
                : accountService.findAllAccountDtosByUser(userId)
        )
    }

    @Log
    @Put("/{id}")
    @Transactional
    Single<AccountDto> edit(@Body AccountUpdateCommand cmd, @NotNull Long id ) {
        Single.just(new AccountDto(accountService.update(cmd, id)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        Account account = accountService.getAccount(id)
        if (transactionService.findAllByAccount(account)) {
            throw new BadRequestException('account.transaction.existence')
        }
        accountService.delete(account)
        HttpResponse.noContent()
    }

}
