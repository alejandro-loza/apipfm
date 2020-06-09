package mx.finerio.pfm.api.controllers


import grails.gorm.transactions.Transactional
import io.micronaut.context.MessageSource
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.AccountDto

import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.validation.AccountCommand

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

    @Post("/")
    Single<AccountDto> save(@Body @Valid AccountCommand cmd){
        Single.just(new AccountDto(accountService.create(cmd)))
    }

    @Get("/{id}")
    @Transactional
    Single<AccountDto> show(@NotNull Long id) {
        Single.just(new AccountDto(accountService.getAccount(id)))
    }

    @Get("{?cursor}")
    @Transactional
    Single<Map> showAll(@Nullable Long cursor) {
        List<AccountDto> accounts = cursor ? accountService.findAllByCursor(cursor) : accountService.getAll()
        Single.just(accounts.isEmpty() ? [] :  new ResourcesDto(accounts)) as Single<Map>
    }

    @Put("/{id}")
    @Transactional
    Single<AccountDto> edit(@Body @Valid AccountCommand cmd, @NotNull Long id ) {
        Single.just(new AccountDto(accountService.update(cmd, id)))
    }

    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        accountService.delete(id)
        HttpResponse.noContent()
    }

}