package mx.finerio.pfm.api.dtos.resource

import groovy.transform.ToString

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.dtos.resource.ResourceDto

@ToString(includeNames = true, includePackage = false)
class BudgetDto extends ResourceDto{
    Long categoryId
    String name
    BigDecimal amount

    BudgetDto(){}

    BudgetDto(Budget budget) {
        this.id = budget.id
        this.categoryId = budget.category.id
        this.name = budget.name
        this.amount = budget.amount
        this.dateCreated = budget.dateCreated
        this.lastUpdated = budget.lastUpdated
    }

}
