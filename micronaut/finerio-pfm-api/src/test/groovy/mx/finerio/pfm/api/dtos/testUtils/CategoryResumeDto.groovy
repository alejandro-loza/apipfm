package mx.finerio.pfm.api.dtos.testUtils

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class TestCategoryResumeDto {
    Long categoryId
    float amount
    float average
    Integer quantity = 0
}
