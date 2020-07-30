package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ToString(includeNames = true, includePackage = false)
class BudgetCreateCommand extends ValidationCommand{

    @NotNull(message= 'user.null')
    Long userId

    @NotNull(message= 'category.null')
    Long  categoryId

    @NotNull(message= 'budget.name.null')
    @NotBlank(message= 'budget.name.blank')
    String name

    @NotNull(message= 'budget.amount.null')
    BigDecimal amount

}
