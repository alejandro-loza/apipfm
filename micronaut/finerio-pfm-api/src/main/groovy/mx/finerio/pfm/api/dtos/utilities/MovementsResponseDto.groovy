package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class MovementsResponseDto {
    List<MovementsAnalysisDto> data

    MovementsResponseDto(List<MovementsAnalysisDto> data) {
        this.data = data
    }
}
