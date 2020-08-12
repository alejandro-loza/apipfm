package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.CategoryDto
import mx.finerio.pfm.api.exceptions.BadRequestException
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

    @Transactional
    @Override
    Category create(CategoryCreateCommand cmd){
        verifyBody(cmd)
        Category category = new Category()
        category.with {
            name = cmd.name
            color = cmd.color
            client = getCurrentLoggedClient()
        }
        category.parent = findParentCategory(cmd)
        category.user = findUser(cmd)
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
        if(!category.isSubcategory()){
            categoryGormService.deleteAllByParentCategory(category)
        }
        category.dateDeleted = new Date()
        categoryGormService.save(category)
    }

    @Override
    List<CategoryDto> findAllByCurrentLoggedClientAndUserNul() {
        categoryGormService
                .findAllByClientAndUserIsNullAndDateDeletedIsNull(
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
        !parentCategoryId ? null : getValidParentCategory(parentCategoryId)
    }

    private Category getValidParentCategory(long parentCategoryId) {
        Category parentCategory = getById(parentCategoryId)
        if(parentCategory.parent){
            throw new BadRequestException('category.parentCategory.invalid')
        }
        parentCategory
    }

    private User findUser(CategoryCreateCommand cmd) {
        !cmd.userId ? null : userService.getUser(cmd.userId)
    }
}
