package mx.finerio.pfm.api.services


import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import mx.finerio.pfm.api.validation.CategoryUpdateCommand

interface CategoryService {
    Category create(CategoryCreateCommand cmd)
    Category getById(Long id)
    Category update(CategoryUpdateCommand cmd, Long id)
    void delete(Long id)
    List<CategoryDto> findAllByCurrentLoggedClient()
    List<CategoryDto> findAllByUser(User user)
}