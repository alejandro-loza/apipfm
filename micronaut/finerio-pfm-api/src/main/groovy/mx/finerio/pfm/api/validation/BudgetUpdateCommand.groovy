package mx.finerio.pfm.api.validation

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class BudgetUpdateCommand extends ValidationCommand{

    Long userId
    Long  categoryId
    String name
    BigDecimal amount
    BigDecimal warningPercentage
}
