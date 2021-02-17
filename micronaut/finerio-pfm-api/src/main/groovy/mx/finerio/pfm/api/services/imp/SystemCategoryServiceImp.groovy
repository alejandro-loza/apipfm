package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.dtos.resource.CategoryDto
import mx.finerio.pfm.api.services.SystemCategoryService
import mx.finerio.pfm.api.services.gorm.SystemCategoryGormService

import javax.inject.Inject

class SystemCategoryServiceImp extends ServiceTemplate implements SystemCategoryService {

    @Inject
    SystemCategoryGormService systemCategoryGormService

    @Override
    List<CategoryDto> findAll() {
        systemCategoryGormService.findAll()
                .collect{ SystemCategory systemCategory ->
                    generateDto(systemCategory)
                }
    }

    @Override
    SystemCategory find(Long id) {
        systemCategoryGormService.findByIdAndDateDeletedIsNull(id)
    }

    @Override
    SystemCategory findByFinerioConnectId(String finnerioConnectId) {
        systemCategoryGormService.findByFinerioConnectId(finnerioConnectId)
    }

    static CategoryDto generateDto(systemCategory) {
        CategoryDto category = new CategoryDto()
        category.with {
            id = systemCategory.id
            name = systemCategory.name
            color = systemCategory.color
            parentCategoryId = systemCategory.parent?.id
            dateCreated = systemCategory.dateCreated
            lastUpdated = systemCategory.lastUpdated
            dateCreated = systemCategory.dateDeleted
        }
        category
    }

}
