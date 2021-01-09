package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class BaseCategoryResumeDto {
    Long categoryId
    float amount
    float average
    Integer quantity = 0
}
