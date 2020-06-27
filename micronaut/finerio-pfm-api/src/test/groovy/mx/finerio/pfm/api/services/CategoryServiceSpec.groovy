package mx.finerio.pfm.api.services

import io.micronaut.security.utils.SecurityService
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.imp.CategoryServiceImp
import mx.finerio.pfm.api.validation.CategoryCommand
import spock.lang.Specification

import java.security.Principal

import static java.util.Optional.of

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
        CategoryCommand cmd = generateCommand()
        cmd.parentCategoryId = 888
        def user = new User()

        when:
        1 * categoryService.userService.getUser(_ as Long) >> user
        1 * categoryService.securityService.getAuthentication() >> of(Principal)
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull(_ as Long ) >> null
        0 * categoryService.categoryGormService.save()

        categoryService.create(cmd)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'category.notFound'
    }

    def 'Should save an category with parent category'(){
        given:'an category command request body'
        CategoryCommand cmd = generateCommand()
        def user = new User()
        def parentCategory = new Category(cmd, user, new Client())
        parentCategory.id = 1234
        cmd.parentCategoryId = parentCategory.id
        def category = new Category(cmd, user, new Client())
        category.parent = parentCategory

        when:
        1 * categoryService.userService.getUser(_ as Long) >> user
        1 * categoryService.securityService.getAuthentication() >> of(Principal)
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull( _ as Long) >> parentCategory
        1 * categoryService.categoryGormService.save(_  as Category) >> category

        Category response = categoryService.create(cmd)

        then:
        response instanceof Category
        assert response.parent == parentCategory
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

        def result = categoryService.find(1L)

        then:
        result instanceof Category
    }

    def "Should not get a category and throw exception"(){

        when:
        1 * categoryService.categoryGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        categoryService.find(666)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'category.notFound'
    }

    def "Should get all categories" () {
        def category = new Category()
        category.user = new User()
        when:
        1 * categoryService.categoryGormService.findAllByDateDeletedIsNull(_ as Map) >> [category]
        def response = categoryService.getAll()

        then:
        response instanceof  List<Category>
    }

    def "Should not get all categories" () {
        when:
        1 * categoryService.categoryGormService.findAllByDateDeletedIsNull(_ as Map) >> []
        def response = categoryService.getAll()

        then:
        response instanceof  List<Category>
        response.isEmpty()
    }

    def "Should get categories by a cursor " () {
        given:
        def category = new Category()
        category.user = new User()
        when:
        1 * categoryService.categoryGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(_ as Long, _ as Map) >> [category]
        def response = categoryService.findAllByCursor(2)

        then:
        response instanceof  List<org.junit.experimental.categories.Category>
    }

    private CategoryCommand generateCommand() {
        CategoryCommand cmd = new CategoryCommand()
        cmd.with {
            userId = 123
            name = "Ropa y Calzado"
            color = "#00FFAA"
        }
        cmd
    }



}
