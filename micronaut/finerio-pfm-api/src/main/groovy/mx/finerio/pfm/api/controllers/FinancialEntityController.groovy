package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.validation.FinancialEntityCreateCommand
import mx.finerio.pfm.api.validation.FinancialEntityUpdateCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static io.reactivex.Single.just

@Controller("/financialEntities")
@Validated
@Secured('isAuthenticated()')
class FinancialEntityController {

    @Inject
    FinancialEntityService financialEntityService

    @Post("/")
    Single<FinancialEntityDto> save(@Body @Valid FinancialEntityCreateCommand cmd){
        just(new FinancialEntityDto(financialEntityService.create(cmd)))
    }

    @Get("/{id}")
    @Transactional
    Single<FinancialEntityDto> show(@NotNull Long id) {
        just(new FinancialEntityDto(financialEntityService.getById(id)))
    }

    @Put("/{id}")
    Single<FinancialEntityDto> edit(@Body FinancialEntityUpdateCommand cmd, @NotNull Long id ) {
        just(new FinancialEntityDto(financialEntityService.update(cmd,id)))
    }

    @Get("{?cursor}")
    Single<ResourcesDto> showAll(@Nullable Long cursor) {
        List<FinancialEntityDto> entities = cursor
                ? financialEntityService.findAllByCursor(cursor)
                : financialEntityService.getAll()
        just( new ResourcesDto(entities))
    }

    @Delete("/{id}")
    HttpResponse delete(@NotNull Long id) {
        financialEntityService.delete(id)
        HttpResponse.noContent()
    }

}
