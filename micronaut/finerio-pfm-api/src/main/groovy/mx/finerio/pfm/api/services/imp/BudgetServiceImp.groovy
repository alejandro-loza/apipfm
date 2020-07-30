package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.BudgetService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand

import javax.inject.Inject

class BudgetServiceImp extends ServiceTemplate implements BudgetService {

    @Inject
    BudgetGormService budgetGormService

    @Inject
    UserService userService

    @Inject
    CategoryService categoryService

    @Override
    Budget create(BudgetCreateCommand cmd){
        verifyBody(cmd)
        User user = userService.getUser(cmd.userId)
        budgetGormService.save(new Budget(cmd, user, findCategoryToSet(cmd.categoryId, user)))
    }

    @Override
    Budget find(Long id) {
        Optional.ofNullable(budgetGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('budget.notFound') })
    }

    @Override
    Budget update(BudgetUpdateCommand cmd, Long id){
        verifyBody(cmd)
        Budget budget = find(id)
        Category categoryToSet = cmd.categoryId ? findCategoryToSet(cmd.categoryId, budget.user) : budget.category
        budget.with {
            user = cmd.userId ? userService.getUser(cmd.userId): budget.user
            category = categoryToSet
            name = cmd.name ?: budget.name
            amount = cmd.amount ?: budget.amount
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
        budgetGormService
                .findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new BudgetDto(it)}
    }

    @Override
    List<BudgetDto> findAllByUserAndCursor(Long userId, Long cursor) {
        User user = userService.getUser(userId)
        verifyLoggedClient(user.client)
        budgetGormService
                .findAllByUserAndIdLessThanEqualsAndDateDeletedIsNull(
                        user, cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new BudgetDto(it)}
    }

    @Override
    List<BudgetDto> findAllByUser(Long userId) {
        User user = userService.getUser(userId)
        verifyLoggedClient(user.client)
        budgetGormService
                .findAllByUserAndDateDeletedIsNull(user, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new BudgetDto(it)}
    }

    private void verifyLoggedClient(Client client) {
        if (client.id != getCurrentLoggedClient().id) {
            throw new ItemNotFoundException('account.notFound')
        }
    }

    private Category findCategoryToSet(Long categoryId, User user) {
        Category categoryToSet = categoryService.getById(categoryId)
        if (categoryToSet
                && budgetGormService.findByUserAndCategoryAndDateDeletedIsNull(user, categoryToSet)) {
            throw new BadRequestException('budget.category.nonUnique')
        }
        categoryToSet
    }

}
