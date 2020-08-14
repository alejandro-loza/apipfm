package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.security.utils.SecurityService
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.imp.CategoryServiceImp
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import spock.lang.Specification

import java.security.Principal

import static java.util.Optional.of

@Property(name = 'spec.name', value = 'category service')
@MicronautTest(application = Application.class)
class CategoryServiceSpec extends Specification {

    CategoryService categoryService = new CategoryServiceImp()

    void setup(){
        categoryService.categoryGormService = Mock(CategoryGormService)
        categoryService.userService = Mock(UserService)
        categoryService.securityService = Mock(SecurityService)
        categoryService.clientService = Mock(ClientService)
    }

    def 'Should not save an category with parent category on parent category not found'(){

        given:'an category command request body'

        def user = new User()
        CategoryCreateCommand cmd = generateCommand()
        cmd.parentCategoryId = 888
        cmd.userId = user.id

        when:
        1 * categoryService.securityService.getAuthentication() >> of(Principal)
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull(_ as Long ) >> null
        0 * categoryService.userService.getUser(_ as Long)
        0 * categoryService.categoryGormService.save()

        categoryService.create(cmd)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'category.notFound'
    }

    def 'Should save an category with parent category'(){
        given:'an category command request body'
        CategoryCreateCommand cmd = generateCommand()
        def user = new User()
        def parentCategory = new Category(cmd, new Client())
        parentCategory.id = 1234
        cmd.parentCategoryId = parentCategory.id
        def category = new Category(cmd, new Client())
        category.parent = parentCategory

        when:
        1 * categoryService.securityService.getAuthentication() >> of(Principal)
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull( _ as Long) >> parentCategory
        1 * categoryService.categoryGormService.save(_  as Category) >> category

        Category response = categoryService.create(cmd)

        then:
        response instanceof Category
        assert response.parent == parentCategory
    }

    def 'Should NOT save an category with parent category that is a subcategory'(){
        given:'an category command request body'
        CategoryCreateCommand cmd = generateCommand()
        def user = new User()
        and:'a pseudo parent category'
        Category pseudoParentCategory = new Category(cmd, new Client())
        pseudoParentCategory.id = 1234
        pseudoParentCategory.parent = new Category()
        cmd.parentCategoryId = pseudoParentCategory.id

        Category category = new Category(cmd, new Client())
        category.parent = pseudoParentCategory

        when:
        1 * categoryService.securityService.getAuthentication() >> of(Principal)
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull( _ as Long) >> pseudoParentCategory
        0 * categoryService.categoryGormService.save(_  as Category) >> category

        categoryService.create(cmd)

        then:
        BadRequestException e = thrown()
        e.message == 'category.parentCategory.invalid'
    }

    def "Should throw exception on null body"() {

        when:
        categoryService.create(null)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should get a category"(){

        when:
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull(_ as Long) >> new Category()

        def result = categoryService.getById(1L)

        then:
        result instanceof Category
    }

    def "Should not get a category and throw exception"(){

        when:
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        categoryService.getById(666)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'category.notFound'
    }

    def "Should get all categories" () {
        def category = new Category()
        category.user = new User()

        when:
        1 * categoryService.clientService.findByUsername(_ as String) >>  new Client()
        1 * categoryService.securityService.getAuthentication() >> of(Principal)
        1 * categoryService.categoryGormService.findAllByClientAndUserIsNullAndDateDeletedIsNull(_ as Client, _ as Map) >> [category]

        def response = categoryService.findAllByCurrentLoggedClientAndUserNul()

        then:
        response instanceof  List<Category>
    }

    def "Should not get all categories" () {
        when:
        1 * categoryService.clientService.findByUsername(_ as String) >>  new Client()
        1 * categoryService.securityService.getAuthentication() >> of(Principal)
        1 * categoryService.categoryGormService.findAllByClientAndUserIsNullAndDateDeletedIsNull(_ as Client , _ as Map) >> []
        def response = categoryService.findAllByCurrentLoggedClientAndUserNul()

        then:
        response instanceof  List<Category>
        response.isEmpty()
    }

    def "Should delete a category"(){

        Category category = new Category()
        category.parent = new Category()

        when:

        1 * categoryService.categoryGormService.save(_ as Category)

        def response = categoryService.delete(category)

        then:
        !response

    }

    def "Should delete a category and its categories"(){

        Category category = new Category()

        when:
        1 * categoryService.categoryGormService.deleteAllByParentCategory(category)
        1 * categoryService.categoryGormService.save(_ as Category)

        def response = categoryService.delete(category)

        then:
        !response

    }

    private static CategoryCreateCommand generateCommand() {
        CategoryCreateCommand cmd = new CategoryCreateCommand()
        cmd.with {
            userId = 123
            name = "Ropa y Calzado"
            color = "#00FFAA"
        }
        cmd
    }



}
