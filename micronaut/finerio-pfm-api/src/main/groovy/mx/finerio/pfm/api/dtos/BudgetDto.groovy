package mx.finerio.pfm.api.dtos

import groovy.transform.ToString

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User

@ToString(includeNames = true, includePackage = false)
class BudgetDto {
    Long id
    Long userId
    Long categoryId
    String name
    Float amount
    Long parentBudgetId
    Date dateCreated
    Date lastUpdated

    BudgetDto(){}

    BudgetDto(Budget budget) {
        this.id = budget.id
        this.userId = budget.user.id
        this.categoryId = budget.category.id
        this.name = budget.name
        this.amount = budget.amount
        this.parentBudgetId = budget.parentBudgetId
        this.dateCreated = budget.dateCreated
        this.lastUpdated = budget.lastUpdated
    }

}
