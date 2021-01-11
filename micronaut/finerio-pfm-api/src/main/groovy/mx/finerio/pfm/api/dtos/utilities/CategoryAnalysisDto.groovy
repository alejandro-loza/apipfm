package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class CategoryAnalysisDto extends BaseCategoryResumeDto {
    float average
    Integer quantity = 0
    List<SubCategoryResumeDto> subcategories
}
