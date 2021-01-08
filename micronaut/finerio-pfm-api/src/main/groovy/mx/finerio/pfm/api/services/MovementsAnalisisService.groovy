package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand

interface MovementsAnalisisService {
    @Log
    List<MovementsDto>  getAnalysis(Long userId, MovementAnalysisFilterParamsCommand cmd)
}