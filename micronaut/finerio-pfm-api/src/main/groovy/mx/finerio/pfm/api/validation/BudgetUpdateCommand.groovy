package mx.finerio.pfm.api.validation

import javax.validation.constraints.Positive

class BudgetUpdateCommand extends ValidationCommand{

    Long userId
    Long  categoryId
    String name
    @Positive
    Float amount
    Long parentBudgetId

}
