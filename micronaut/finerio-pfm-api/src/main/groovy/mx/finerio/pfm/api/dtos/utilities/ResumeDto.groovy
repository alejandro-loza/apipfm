package mx.finerio.pfm.api.dtos.utilities

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString

@JsonInclude(JsonInclude.Include.ALWAYS)
@ToString(includeNames = true, includePackage = false)
class ResumeDto {
    List<MovementsResumeDto> incomes = []
    List<MovementsResumeDto> expenses = []
    List<BalancesDto> balances = []
}
