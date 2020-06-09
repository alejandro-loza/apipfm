package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import java.awt.Color
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.CategoryCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'account controller')
@MicronautTest(application = Application.class)
class CategoryControllerSpec extends Specification {

    public static final String CATEGORIES_ROOT = "/categories"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    UserGormService userGormService

    @Inject
    CategoryGormService categoryGormService

    def "Should get a empty list of categories"() {

        given: 'a client'
        HttpRequest getReq = HttpRequest.GET(CATEGORIES_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(CategoryDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()
    }

    def "Should create a category"() {
        given: 'an saved User '
        User user1 = generateUser()

        and: 'a command request body'
        CategoryCommand cmd = generateCategoryCommand(user1)

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd)

        when:
        def rsp = client.toBlocking().exchange(request, CategoryDto)

        then:
        rsp.status == HttpStatus.OK
        assert rsp.body().with {
            assert cmd
            assert id
            assert dateCreated
            assert lastUpdated
        }

    }

    def "Should not create a category and throw bad request on wrong params"() {
        given: 'a category request body with empty body'

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, new CategoryCommand())

        when:
        client.toBlocking().exchange(request, CategoryDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create a transaction and throw bad request on wrong body"() {
        given: 'a transaction request body with empty body'

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, 'asd')

        when:
        client.toBlocking().exchange(request, CategoryDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create a category and throw not found exception on user not found"() {
        given: 'an account request body with no found account id'

        def user = new User()
        user.id = 666
        CategoryCommand cmd = generateCategoryCommand(user)

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
    }

    def "Should get a category"() {
        given: 'a saved user'
        User user = generateUser()

        and: 'a saved category'
        Category category = new Category(generateCategoryCommand(user), user)
        categoryGormService.save(category)

        and:
        HttpRequest getReq = HttpRequest.GET(CATEGORIES_ROOT + "/${category.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, CategoryDto)

        then:
        rspGET.status == HttpStatus.OK
        assert rspGET.body().with {
            category
        }
        assert !category.dateDeleted

    }

    def "Should not get a transaction and throw 404"() {//TODO test the error body
        given: 'a not found id request'

        HttpRequest request = HttpRequest.GET("${CATEGORIES_ROOT}/0000")

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(NotFoundException))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should not get an account and throw 400"() {
        given: 'a not found id request'

        HttpRequest request = HttpRequest.GET("${CATEGORIES_ROOT}/abc")

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should update an category"() {
        given: 'a saved user'
        User user1 = generateUser()

        and: 'a saved category'
        Category category = new Category()
        category.with {
            user = user1
            name = 'awesome name'
            color = Color.decode('#12AD4D')
            parentCategoryId = 456
        }
        categoryGormService.save(category)

        and: 'an account command to update data'
        def cmd = generateCategoryCommand(user1)

        and: 'a client'
        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${category.id}", cmd)

        when:
        def resp = client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>,
                Argument.of(ErrorDto))
        then:
        resp.status == HttpStatus.OK
        resp.body().with {
           cmd
        }

    }

    def "Should not update a transaction on band parameters and return Bad Request"() {
        given: 'a saved user'
        User user1 = generateUser()

        and: 'a saved category'
        Category category = new Category()
        category.with {
            user = user1
            name = 'awesome name'
            color = Color.decode('#12AD4D')
            parentCategoryId = 456
        }
        categoryGormService.save(category)


        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${category.id}", [])

        when:
        client.toBlocking().exchange(request,  Argument.of(CategoryDto) as Argument<CategoryDto>,
                Argument.of(ErrorDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update a category and throw not found exception"() {
        given:
        def notFoundId = 666

        and: 'a client'
        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${notFoundId}",
                generateCategoryCommand(generateUser()))

        when:
        client.toBlocking().exchange(request, CategoryDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of categories"() {

        given: 'a category list'
        User user1 = generateUser()

        Category category1 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category1)

        Category category2 = new Category(generateCategoryCommand(user1), user1)
        category2.dateDeleted = new Date()
        categoryGormService.save(category2)

        Category category3 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category3)

        Category category4 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category4)

        Category category5 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category5)

        and:
        HttpRequest getReq = HttpRequest.GET(CATEGORIES_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<CategoryDto> categoryDtos = body.get("data") as List<CategoryDto>
        assert !(category2.id in categoryDtos.id)

        assert !body.get("nextCursor")
    }

    def "Should get a list of categories in a cursor "() {

        given: 'a category list'
        User user1 = generateUser()

        Category category1 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category1)

        Category category2 = new Category(generateCategoryCommand(user1), user1)
        category2.dateDeleted = new Date()
        categoryGormService.save(category2)

        Category category3 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category3)

        Category category4 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category4)

        Category category5 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category5)

        and:
        HttpRequest getReq = HttpRequest.GET("$CATEGORIES_ROOT?cursor=${category4.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<CategoryDto> categoryDtos = body.get("data") as List<CategoryDto>
        assert !(category2.id in categoryDtos.id)
        assert !(category5.id in categoryDtos.id)

        assert !body.get("nextCursor")
    }


    def "Should throw not found exception on delete no found category"() {
        given:
        def notFoundId = 666

        and: 'a client'
        HttpRequest request = HttpRequest.DELETE("${CATEGORIES_ROOT}/${notFoundId}")

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(NotFoundException))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete a category"() {
        given: 'a saved category'
        User user1 = generateUser()

        Category category1 = new Category(generateCategoryCommand(user1), user1)
        categoryGormService.save(category1)

        and: 'a client request'
        HttpRequest request = HttpRequest.DELETE("${CATEGORIES_ROOT}/${category1.id}")

        when:
        def response = client.toBlocking().exchange(request, CategoryDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("${CATEGORIES_ROOT}/${category1.id}")

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>,
                Argument.of(NotFoundException))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    private User generateUser() {
        User user = new User()
        user.with {
            name = 'awesome name'

        }
        userGormService.save(user)
        user
    }

    private static CategoryCommand generateCategoryCommand(User user1) {
        CategoryCommand cmd = new CategoryCommand()
        cmd.with {
            userId = user1.id
            name = 'Shoes and clothes'
            color = "#00FFAA"
            parentCategoryId = 123
        }
        cmd
    }

}