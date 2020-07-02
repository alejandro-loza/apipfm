package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.validation.CategoryCommand

import java.awt.Cursor

interface CategoryService {
    Category create(CategoryCommand cmd)
    Category getById(Long id)
    Category update(CategoryCommand cmd, Long id)
    void delete(Long id)
    List<CategoryDto> findAllByCurrentLoggedClient()
    List<CategoryDto> findAllByUser(User user)
}