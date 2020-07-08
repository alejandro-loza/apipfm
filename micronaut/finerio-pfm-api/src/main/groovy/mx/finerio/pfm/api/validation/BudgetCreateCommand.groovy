package mx.finerio.pfm.api.validation

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

class BudgetCreateCommand extends ValidationCommand{

    @NotNull(message= 'user.null')
    Long userId

    @NotNull(message= 'category.null')
    Long  categoryId

    @NotNull(message= 'budget.name.null')
    @NotBlank(message= 'budget.name.blank')
    String name

    @NotNull(message= 'budget.amount.null')
    @Positive
    Float amount

    @NotNull(message= 'budget.parentBudget.null')
    Long parentBudgetId

}
