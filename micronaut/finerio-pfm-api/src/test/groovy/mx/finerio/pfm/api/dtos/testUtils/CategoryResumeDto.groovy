package mx.finerio.pfm.api.dtos.testUtils

import groovy.transform.ToString
import mx.finerio.pfm.api.dtos.utilities.BaseCategoryResumeDto
import mx.finerio.pfm.api.dtos.utilities.SubCategoryResumeDto

@ToString(includeNames = true, includePackage = false)
class TestCategoryResumeDto {
    Long categoryId
    float amount
    float average
    Integer quantity = 0
}
