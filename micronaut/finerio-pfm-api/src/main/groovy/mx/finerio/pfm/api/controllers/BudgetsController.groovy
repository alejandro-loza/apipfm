package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.BudgetService
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller("/budgets")
@Validated
@Secured('isAuthenticated()')
class BudgetsController {

    @Inject
    BudgetService budgetService

    @Log
    @Post("/")
    Single<BudgetDto> save(@Body @Valid BudgetCreateCommand cmd){
        Single.just(new BudgetDto(budgetService.create(cmd)))
    }

    @Log
    @Get("/{id}")
    @Transactional
    Single<BudgetDto> show(@NotNull Long id) {
        Single.just(new BudgetDto(budgetService.find(id)))
    }

    @Log
    @Get("{?cursor}")
    @Transactional
    Single<ResourcesDto> showAll(@Nullable Long cursor,  @QueryValue('userId') Long userId) {
        Single.just(new ResourcesDto( cursor
                ? budgetService.findAllByUserAndCursor(userId, cursor)
                : budgetService.findAllByUser(userId)
       ))
    }

    @Log
    @Put("/{id}")
    @Transactional
    Single<BudgetDto> edit(@Body @Valid BudgetUpdateCommand cmd, @NotNull Long id ) {
        Single.just(new BudgetDto(budgetService.update(cmd, id)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        budgetService.delete(id)
        HttpResponse.noContent()
    }

}
