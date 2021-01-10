package mx.finerio.pfm.api.dtos.testUtils

import groovy.transform.ToString
import mx.finerio.pfm.api.dtos.utilities.BaseCategoryResumeDto
import mx.finerio.pfm.api.dtos.utilities.CategoryResumeDto

class MovementsTestDto {
    Long date
    float amount
    float average
    Integer quantity = 0
    List<CategoryResumeDto> categories
}
