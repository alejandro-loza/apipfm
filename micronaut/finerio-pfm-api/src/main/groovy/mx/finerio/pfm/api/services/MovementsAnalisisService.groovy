package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.utilities.MovementsResumeDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.MovementAnalysisFilterParamsCommand

interface MovementsAnalisisService {
    @Log
    List<MovementsResumeDto>  getAnalysis(Long userId, MovementAnalysisFilterParamsCommand cmd)
}