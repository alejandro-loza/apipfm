package mx.finerio.pfm.api.dtos

import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.validation.CategoryCommand
import spock.lang.Specification
import mx.finerio.pfm.api.domain.Category

@MicronautTest(application = Application.class)
class CategoryDtoSpec extends Specification{

    def "Should build an category"(){
        setup:
        User user = new User()
        user.with {
           name = 'test'
            id = 666
        }
        CategoryCommand cmd = new CategoryCommand()
        cmd.with {
            userId = user.id
            name = 'test category'
            color = 'a beautiful one'
            parentCategoryId = 666
        }
        Category category = new Category(cmd, user, new Client())
        category.with {
            parent = new Category(cmd, user, new Client())
        }
        expect:
        category.parent

        when:
        CategoryDto dto = new CategoryDto(category)

        then:
        assert dto

    }

    def "Should build an category without parent"(){
        setup:
        User user = new User()
        user.with {
            name = 'test'
            id = 666
        }
        CategoryCommand cmd = new CategoryCommand()
        cmd.with {
            userId = user.id
            name = 'test category'
            color = 'a beautiful one'
        }
        Category category = new Category(cmd, user, new Client())

        expect:
        assert !category.parent

        when:
        CategoryDto dto = new CategoryDto(category)

        then:
        assert   dto
        assert  !dto.parentCategoryId

    }

}
