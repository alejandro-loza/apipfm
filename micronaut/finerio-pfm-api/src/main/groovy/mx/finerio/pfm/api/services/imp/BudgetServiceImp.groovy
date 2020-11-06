package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.BudgetService
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.SystemCategoryService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject

class BudgetServiceImp extends ServiceTemplate implements BudgetService {

    @Inject
    BudgetGormService budgetGormService

    @Inject
    UserService userService

    @Inject
    CategoryService categoryService

    @Inject
    SystemCategoryService systemCategoryService

    @Override
    Budget create(BudgetCreateCommand cmd){
        Budget budget = new Budget()
        User userToSet = userService.getUser(cmd.userId)
        budget.with {
            user = userToSet
            name = cmd.name
            amount = cmd.amount
        }
        setCategoryOrSystemCategory(cmd, budget, userToSet)
        budgetGormService.save(budget)
    }

    void setCategoryOrSystemCategory(ValidationCommand cmd, Budget budget, User userToSet) {
        Long categoryId = cmd["categoryId"] as Long
        if (categoryId) {
            SystemCategory systemCategory = systemCategoryService.find(categoryId)
            if (systemCategory) {
                budget.systemCategory = systemCategory
            } else {
                budget.category = findCategoryToSet(categoryId, userToSet)
            }
        }
    }

    @Override
    Budget find(Long id) {
        Optional.ofNullable(budgetGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('budget.notFound') })
    }

    @Override
    Budget update(BudgetUpdateCommand cmd, Budget budget){
        User userToSet = cmd.userId ? userService.getUser(cmd.userId) : budget.user

        budget.with {
            user = userToSet
            name = cmd.name ?: budget.name
            amount = cmd.amount ?: budget.amount
        }
        setCategoryOrSystemCategory(cmd, budget, userToSet)

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
    List<BudgetDto> findAllByUserId(Long userId) {
        findAllByUser(userService.getUser(userId))
    }

    @Override
    List<BudgetDto>findAllByUser(User user) {
        verifyLoggedClient(user.client)
        budgetGormService
           .findAllByUserAndDateDeletedIsNull(user, [max: MAX_ROWS, sort: 'id', order: 'desc'])
           .collect { new BudgetDto(it) }
    }

    @Override
    Budget findByUserAndCategory(User user, Category category){
        budgetGormService.findByUserAndCategoryAndDateDeletedIsNull(user, category)
    }

    @Override
    Budget findByCategory(Category category) {
        budgetGormService.findByCategoryAndDateDeletedIsNull(category)
    }

    private void verifyLoggedClient(Client client) {
        if (client.id != getCurrentLoggedClient().id) {
            throw new ItemNotFoundException('account.notFound')
        }
    }

    private Category findCategoryToSet(Long categoryId, User user) {
        Category categoryToSet = categoryService.getById(categoryId)
        if (categoryToSet
                && this.findByUserAndCategory(user, categoryToSet)) {
            throw new BadRequestException('budget.category.nonUnique')
        }
        categoryToSet
    }

}
