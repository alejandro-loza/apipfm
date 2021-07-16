package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.resource.CategorizerDto
import mx.finerio.pfm.api.dtos.resource.CleanerDto
import mx.finerio.pfm.api.logging.Log

interface CategorizerService {

    @Log
    CategorizerDto searchCategory(String description, Boolean income, Boolean clean)

    @Log
    CleanerDto cleanText(String description, Boolean income)

}
