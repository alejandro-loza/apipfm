package mx.finerio.pfm.api.dtos.testUtils

import mx.finerio.pfm.api.dtos.utilities.CategoryAnalysisDto

class MovementsAnalysisTestDto {
    Long date
    float amount
    float average
    Integer quantity = 0
    List<CategoryAnalysisDto> categories
}
