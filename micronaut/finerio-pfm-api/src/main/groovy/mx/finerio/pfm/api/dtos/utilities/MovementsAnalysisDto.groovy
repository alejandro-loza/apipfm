package mx.finerio.pfm.api.dtos.utilities

class MovementsAnalysisDto extends MovementsResumeDto {
    float average
    Integer quantity = 0
    List<BaseCategoryResumeDto> categories = []
}
