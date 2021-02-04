package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.BudgetService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.NextCursorService
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

    @Inject
    CategoryService categoryService

    @Inject
    NextCursorService nextCursorService

    @Log
    @Post("/")
    Single<BudgetDto> save(@Body @Valid BudgetCreateCommand cmd){
        budgetService.verifyBody(cmd)
        Single.just(budgetService.create(cmd))
    }

    @Log
    @Get("/{id}")
    @Transactional
    Single<BudgetDto> show(@NotNull Long id) {
        Single.just(budgetService.get(id))
    }

    @Log
    @Get("{?cursor}")
    @Transactional
    Single<ResourcesDto> showAll(@Nullable Long cursor,  @QueryValue('userId') Long userId) {
        nextCursorService.generateResourcesDto(cursor
                ? budgetService.findAllByUserAndCursor(userId, cursor)
                : budgetService.findAllByUserId(userId)
        )
    }

    @Log
    @Put("/{id}")
    @Transactional
    Single<BudgetDto> edit(@Body @Valid BudgetUpdateCommand cmd, @NotNull Long id ) {
        budgetService.verifyBody(cmd)
        Single.just(budgetService.update(cmd,  budgetService.find(id)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        budgetService.delete(id)
        HttpResponse.noContent()
    }

}
