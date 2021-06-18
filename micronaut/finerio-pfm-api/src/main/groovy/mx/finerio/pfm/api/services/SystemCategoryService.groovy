package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.dtos.resource.CategoryDto
import mx.finerio.pfm.api.logging.Log

interface SystemCategoryService {
    @Log
    List<CategoryDto> findAll()

    @Log
    SystemCategory find(Long id)

    @Log
    SystemCategory findByFinerioConnectId(String finnerioConnectId)
}