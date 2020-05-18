package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.validation.FinancialEntityCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static io.reactivex.Single.just

@Controller("/financialEntities")
@Validated
class FinancialEntityController {

    @Inject
    FinancialEntityService financialEntityService

    @Post("/")
    Single<FinancialEntityDto> save(@Body @Valid FinancialEntityCommand cmd){
        just(new FinancialEntityDto(financialEntityService.create(cmd)))
    }

    @Get("/{id}")
    @Transactional
    Single<FinancialEntityDto> show(@NotNull Long id) {
        just(new FinancialEntityDto(financialEntityService.getById(id)))
    }

    @Put("/{id}")
    Single<FinancialEntityDto> edit(@Body @Valid FinancialEntityCommand cmd, @NotNull Long id ) {
        just(new FinancialEntityDto(financialEntityService.update(cmd,id)))
    }

    @Get("{?cursor}")
    Single<Map> showAll(@Nullable Long cursor) {
        List<FinancialEntity> entities = cursor ? financialEntityService.findAllByCursor(cursor) : financialEntityService.getAll()
        just(entities.isEmpty() ? [] :  new ResourcesDto(entities)) as Single<Map>
    }

    @Delete("/{id}")
    HttpResponse delete(@NotNull Long id) {
        financialEntityService.delete(id)
        HttpResponse.noContent()
    }

}
