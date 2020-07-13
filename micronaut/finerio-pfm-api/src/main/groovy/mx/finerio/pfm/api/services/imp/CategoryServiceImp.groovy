package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import mx.finerio.pfm.api.validation.CategoryUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject

class CategoryServiceImp extends ServiceTemplate implements CategoryService {

    @Inject
    CategoryGormService categoryGormService

    @Inject
    UserService userService

    @Override
    Category create(CategoryCreateCommand cmd){
        verifyBody(cmd)
        Category category = new Category(cmd, findUser(cmd), getCurrentLoggedClient())
        category.parent = findParentCategory(cmd)
        categoryGormService.save(category)
    }

    @Override
    Category getById(Long id) {
        Optional.ofNullable(categoryGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('category.notFound') })
    }

    @Override
    Category update(CategoryUpdateCommand cmd, Long id){
        verifyBody(cmd)
        Category category = getById(id)
        category.with {
            user = cmd.userId ?userService.getUser(cmd.userId) : category.user
            name = cmd.name ?: category.name
            color = cmd.color ?: category.color
        }
        category.parent = findParentCategory(cmd)
        categoryGormService.save(category)
    }

    @Override
    void delete(Long id){
        Category category = getById(id)
        category.dateDeleted = new Date()
        categoryGormService.save(category)
    }

    @Override
    List<CategoryDto> findAllByCurrentLoggedClient() {
        categoryGormService
                .findAllByClientAndDateDeletedIsNull(
                        getCurrentLoggedClient(), [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new CategoryDto(it)}
    }

    @Override
    List<CategoryDto> findAllByUser(User user) {
        categoryGormService.findAllByUserAndDateDeletedIsNull(user, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new CategoryDto(it)}
    }

    private Category findParentCategory(ValidationCommand cmd) {
        Long parentCategoryId = cmd["parentCategoryId"] as Long
        !parentCategoryId ? null : getById(parentCategoryId)
    }

    private User findUser(CategoryCreateCommand cmd) {
        !cmd.userId ? null : userService.getUser(cmd.userId)
    }
}
