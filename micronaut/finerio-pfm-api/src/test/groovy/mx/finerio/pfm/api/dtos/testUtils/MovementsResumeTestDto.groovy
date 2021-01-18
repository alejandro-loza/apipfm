package mx.finerio.pfm.api.dtos.testUtils

import mx.finerio.pfm.api.dtos.utilities.CategoryResumeDto

class MovementsResumeTestDto {
    Long date
    float amount
    float average
    Integer quantity = 0
    List<CategoryResumeDto> categories
}
