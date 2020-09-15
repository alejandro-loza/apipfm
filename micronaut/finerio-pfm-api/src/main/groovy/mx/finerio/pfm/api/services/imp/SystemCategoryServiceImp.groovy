package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.dtos.resource.CategoryDto
import mx.finerio.pfm.api.services.SystemCategoryService
import mx.finerio.pfm.api.services.gorm.SystemCategoryGormService
import mx.finerio.pfm.api.domain.Category
import javax.inject.Inject

class SystemCategoryServiceImp extends ServiceTemplate implements SystemCategoryService {

    @Inject
    SystemCategoryGormService systemCategoryGormService

    @Override
    List<CategoryDto> findAll() {
        systemCategoryGormService.findAll()
                .collect{ SystemCategory systemCategory ->
                    Category category = new Category()
                    category.with {
                        id = systemCategory.id
                        name = systemCategory.name
                        color = systemCategory.color
                        parent = systemCategory.parent
                        dateCreated = systemCategory.dateCreated
                        lastUpdated = systemCategory.lastUpdated
                        dateCreated = systemCategory.dateDeleted
                    }
                    return new CategoryDto(category)
                }
    }

}
