package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class CategoryResumeDto {
    Long categoryId
    float amount
    List<SubCategoryResumeDto> subcategories
}
