package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.validation.CategoryCommand

import javax.inject.Inject

class CategoryServiceImp extends ServiceTemplate implements CategoryService {

    @Inject
    CategoryGormService categoryGormService

    @Inject
    UserService userService

    @Override
    Category create(CategoryCommand cmd){
        verifyBody(cmd)
        categoryGormService.save(new Category(cmd, userService.getUser(cmd.userId)) )
    }

    @Override
    Category find(Long id) {
        Optional.ofNullable(categoryGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new NotFoundException('category.notFound') })
    }

    @Override
    Category update(CategoryCommand cmd, Long id){
        verifyBody(cmd)
        Category category = find(id)
        category.with {
            user = userService.getUser(cmd.userId)
            name = cmd.name
            color = cmd.color
            parentCategoryId = cmd.parentCategoryId
        }
        categoryGormService.save(category)
    }

    @Override
    void delete(Long id){
        Category category = find(id)
        category.dateDeleted = new Date()
        categoryGormService.save(category)
    }

    @Override
    List<CategoryDto> getAll() {
        categoryGormService.findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new CategoryDto(it)}
    }

    @Override
    List<CategoryDto> findAllByCursor(Long cursor) {
        categoryGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new CategoryDto(it)}
    }

}
