package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.validation.TransactionCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller("/transactions")
@Validated
class TransactionController {

    @Inject
    TransactionService transactionsService

    @Post("/")
    Single<TransactionDto> save(@Body @Valid TransactionCommand cmd){
        Single.just(new TransactionDto(transactionsService.create(cmd)))
    }

    @Get("/{id}")
    @Transactional
    Single<TransactionDto> show(@NotNull Long id) {
        Single.just(new TransactionDto(transactionsService.find(id)))
    }

    @Get("{?cursor}")
    @Transactional
    Single<Map> showAll(@Nullable Long cursor) {
        List<TransactionDto> transactions = cursor ? transactionsService.findAllByCursor(cursor) : transactionsService.getAll()
        Single.just(transactions.isEmpty() ? [] :  new ResourcesDto(transactions)) as Single<Map>
    }

    @Put("/{id}")
    @Transactional
    Single<TransactionDto> edit(@Body @Valid TransactionCommand cmd, @NotNull Long id ) {
        Single.just(new TransactionDto(transactionsService.update(cmd, id)))
    }

    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        transactionsService.delete(id)
        HttpResponse.noContent()
    }

}