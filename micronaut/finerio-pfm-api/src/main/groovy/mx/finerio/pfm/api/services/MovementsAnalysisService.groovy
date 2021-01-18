package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.utilities.MovementsAnalysisDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand

interface MovementsAnalysisService {
    @Log
    List<MovementsAnalysisDto>  getAnalysis(Long userId, MovementAnalysisFilterParamsCommand cmd)
}