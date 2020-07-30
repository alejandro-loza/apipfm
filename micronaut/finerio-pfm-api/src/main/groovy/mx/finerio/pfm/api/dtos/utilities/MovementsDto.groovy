package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class MovementsDto {
    Long date
    float amount
    List<CategoryResumeDto> categories
}
