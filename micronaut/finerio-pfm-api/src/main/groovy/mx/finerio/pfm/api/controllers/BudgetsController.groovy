package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.services.BudgetService
import mx.finerio.pfm.api.validation.BudgetCommand
import mx.finerio.pfm.api.validation.CategoryCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller("/budgets")
@Validated
class BudgetsController {

    @Inject
    BudgetService budgetService

    @Post("/")
    Single<BudgetDto> save(@Body @Valid BudgetCommand cmd){
        Single.just(new BudgetDto(budgetService.create(cmd)))
    }

    @Get("/{id}")
    @Transactional
    Single<BudgetDto> show(@NotNull Long id) {
        Single.just(new BudgetDto(budgetService.find(id)))
    }

    @Get("{?cursor}")
    @Transactional
    Single<Map> showAll(@Nullable Long cursor) {
        List<BudgetDto> budgetDtos = cursor ? budgetService.findAllByCursor(cursor) : budgetService.getAll()
        Single.just(budgetDtos.isEmpty() ? [] :  new ResourcesDto(budgetDtos)) as Single<Map>
    }

    @Put("/{id}")
    @Transactional
    Single<BudgetDto> edit(@Body @Valid BudgetCommand cmd, @NotNull Long id ) {
        Single.just(new BudgetDto(budgetService.update(cmd, id)))
    }

    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        budgetService.delete(id)
        HttpResponse.noContent()
    }

}