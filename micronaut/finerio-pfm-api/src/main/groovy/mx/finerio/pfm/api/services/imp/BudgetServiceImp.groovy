package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.BudgetService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.validation.BudgetCommand

import javax.inject.Inject

class BudgetServiceImp extends ServiceTemplate implements BudgetService {

    @Inject
    BudgetGormService budgetGormService

    @Inject
    UserService userService

    @Inject
    CategoryService categoryService

    @Override
    Budget create(BudgetCommand cmd){
        verifyBody(cmd)
        budgetGormService.save(
                new Budget(cmd,
                        userService.getUser(cmd.userId),
                        categoryService.getById(cmd.categoryId))
        )
    }

    @Override
    Budget find(Long id) {
        Optional.ofNullable(budgetGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('budget.notFound') })
    }

    @Override
    Budget update(BudgetCommand cmd, Long id){
        verifyBody(cmd)
        Budget budget = find(id)
        budget.with {
            user = userService.getUser(cmd.userId)
            category = categoryService.getById(cmd.categoryId)
            name = cmd.name
            parentBudgetId = cmd.parentBudgetId
            amount = cmd.amount
        }
        budgetGormService.save(budget)
    }

    @Override
    void delete(Long id){
        Budget budget = find(id)
        budget.dateDeleted = new Date()
        budgetGormService.save(budget)
    }

    @Override
    List<BudgetDto> getAll() {
        budgetGormService.findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new BudgetDto(it)}
    }

    @Override
    List<BudgetDto> findAllByCursor(Long cursor) {
        budgetGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new BudgetDto(it)}
    }

}
