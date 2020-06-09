package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.validation.CategoryCommand

interface CategoryService {
    Category create(CategoryCommand cmd)
    Category find(Long id)
    Category update(CategoryCommand cmd, Long id)
    void delete(Long id)
    List<CategoryDto> getAll()
    List<CategoryDto> findAllByCursor(Long cursor)
}