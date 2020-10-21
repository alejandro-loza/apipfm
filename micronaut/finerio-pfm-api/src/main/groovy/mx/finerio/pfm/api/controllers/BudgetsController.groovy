package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.BudgetService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.NextCursorService
import mx.finerio.pfm.api.services.UserService
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
        Budget budget = budgetService.find(id)
        Category categoryToSet = cmd.categoryId ? findCategoryToSet(cmd.categoryId, budget.user) : budget.category
        Single.just(new BudgetDto(budgetService.update(cmd, budget, categoryToSet)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        budgetService.delete(id)
        HttpResponse.noContent()
    }

    private Category findCategoryToSet(Long categoryId, User user) {
        Category categoryToSet = categoryService.getById(categoryId)
        if (categoryToSet
                && budgetService.findByUserAndCategory(user, categoryToSet)) {
            throw new BadRequestException('budget.category.nonUnique')
        }
        categoryToSet
    }

}
