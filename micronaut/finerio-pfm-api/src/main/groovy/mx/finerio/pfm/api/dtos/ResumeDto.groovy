package mx.finerio.pfm.api.dtos

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class ResumeDto {
    List<MovementsDto> incomes
    List<MovementsDto> expenses
    List<BalancesDto> balances
}
