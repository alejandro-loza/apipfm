package mx.finerio.pfm.api.services


import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.CategoryDto
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
    void delete(Long id)

    @Log
    List<CategoryDto> findAllByCurrentLoggedClient()

    @Log
    List<CategoryDto> findAllByUser(User user)

}
