package mx.finerio.pfm.api.services


import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.CategoryDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import mx.finerio.pfm.api.validation.CategoryUpdateCommand

interface CategoryService {

    @Log
    Category create(CategoryCreateCommand cmd)

    @Log
    Category getById(Long id)

    @Log
    Category update(CategoryUpdateCommand cmd, Long id)

    @Log
    void delete(Category category)

    @Log
    List<CategoryDto> findAllByCurrentLoggedClientAndUserNul()

    @Log
    List<CategoryDto> findAllCategoryDtosByUser(User user)

    @Log
    List<Category> findAllByUser(User user)

    @Log
    List<CategoryDto> findAllByCategory(Category category)

}
