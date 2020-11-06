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
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.resource.FinancialEntityDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.NextCursorService
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

    @Inject
    AccountService accountService

    @Inject
    NextCursorService nextCursorService

    @Log
    @Post("/")
    Single<FinancialEntityDto> save(@Body @Valid FinancialEntityCreateCommand cmd){
        just(new FinancialEntityDto(financialEntityService.create(cmd)))
    }

    @Log
    @Get("/{id}")
    @Transactional
    Single<FinancialEntityDto> show(@NotNull Long id) {
        just(new FinancialEntityDto(financialEntityService.getById(id)))
    }

    @Log
    @Put("/{id}")
    Single<FinancialEntityDto> edit(@Body FinancialEntityUpdateCommand cmd, @NotNull Long id ) {
        just(new FinancialEntityDto(financialEntityService.update(cmd,id)))
    }

    @Log
    @Get("{?cursor}")
    Single<ResourcesDto> showAll(@Nullable Long cursor) {
        nextCursorService.generateResourcesDto( cursor
                ? financialEntityService.findAllByCursor(cursor)
                : financialEntityService.getAll()
        )
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        FinancialEntity entity = financialEntityService.getById(id)
        if(accountService.findAllByFinancialEntity(entity)){
            throw new BadRequestException('financialEntity.account.childExistence')
        }
        financialEntityService.delete(entity)
        HttpResponse.noContent()
    }

}
