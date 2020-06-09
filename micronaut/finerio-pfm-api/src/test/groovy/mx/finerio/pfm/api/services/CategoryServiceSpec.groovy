package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.imp.CategoryServiceImp
import mx.finerio.pfm.api.validation.CategoryCommand
import spock.lang.Specification

class CategoryServiceSpec extends Specification {

    CategoryService categoryService = new CategoryServiceImp()

    void setup(){
        categoryService.categoryGormService = Mock(CategoryGormService)
        categoryService.userService = Mock(UserService)
    }

    def 'Should save an category'(){
        given:'an category command request body'
        CategoryCommand cmd = generateCommand()
        def user = new User()

        when:
        1 * categoryService.userService.getUser(_ as Long) >> user
        1 * categoryService.categoryGormService.save(_  as Category) >> new Category(cmd, user)

        def response = categoryService.create(cmd)

        then:
        response instanceof Category
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
        NotFoundException e = thrown()
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
            parentCategoryId = 123
        }
        cmd
    }



}
