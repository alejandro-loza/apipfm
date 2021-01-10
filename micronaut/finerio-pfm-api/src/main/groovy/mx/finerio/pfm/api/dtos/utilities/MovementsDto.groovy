package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class MovementsDto {
    Long date
    float amount
    float average
    Integer quantity = 0
    List<BaseCategoryResumeDto> categories = []
}
