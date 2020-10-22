package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.dtos.resource.CategoryDto

interface SystemCategoryService {
    List<CategoryDto> findAll()
    SystemCategory find(Long id)
    SystemCategory findByFinerioConnectId(String finnerioConnectId)
}