package mx.finerio.pfm.api.dtos.utilities

import com.fasterxml.jackson.annotation.JsonInclude

import groovy.transform.ToString

import groovy.transform.ToString

@JsonInclude(JsonInclude.Include.ALWAYS)
@ToString(includeNames = true, includePackage = false)
class ResumeDto {
    List<MovementsDto> incomes = []
    List<MovementsDto> expenses = []
    List<BalancesDto> balances = []
}
