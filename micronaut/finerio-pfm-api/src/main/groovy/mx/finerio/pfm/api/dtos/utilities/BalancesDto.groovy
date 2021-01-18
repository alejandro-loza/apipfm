package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class BalancesDto {
    Long date
    float incomes
    float expenses
}
