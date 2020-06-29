package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.validation.CategoryCommand

import javax.inject.Inject
import java.awt.Cursor

class CategoryServiceImp extends ServiceTemplate implements CategoryService {

    @Inject
    CategoryGormService categoryGormService

    @Inject
    UserService userService

    @Override
    Category create(CategoryCommand cmd){
        verifyBody(cmd)
        Category category = new Category(cmd, findUser(cmd), getCurrentLoggedClient())
        category.parent = findParentCategory(cmd)
        categoryGormService.save(category)
    }

    @Override
    Category find(Long id) {
        Optional.ofNullable(categoryGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('category.notFound') })
    }

    @Override
    Category update(CategoryCommand cmd, Long id){
        verifyBody(cmd)
        Category category = find(id)
        category.with {
            user = userService.getUser(cmd.userId)
            name = cmd.name
            color = cmd.color
        }
        category.parent = findParentCategory(cmd)
        categoryGormService.save(category)
    }

    @Override
    void delete(Long id){
        Category category = find(id)
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

    private Category findParentCategory(CategoryCommand cmd) {
        if(!cmd.parentCategoryId){
           return null
        }
        find(cmd.parentCategoryId)
    }

    private User findUser(CategoryCommand cmd) {
        if(!cmd.userId){
            return null
        }
        userService.getUser(cmd.userId)
    }
}
