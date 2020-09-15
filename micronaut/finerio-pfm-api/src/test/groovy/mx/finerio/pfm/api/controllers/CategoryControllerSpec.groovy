package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.dtos.resource.CategorizerDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.CategoryDto
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import mx.finerio.pfm.api.validation.CategoryUpdateCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'account controller')
@MicronautTest(application = Application.class)
class CategoryControllerSpec extends Specification {

    public static final String CATEGORIES_ROOT = "/categories"
    public static final String LOGIN_ROOT = "/login"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    UserGormService userGormService

    @Inject
    @Shared
    CategoryGormService categoryGormService

    @Inject
    @Shared
    ClientService clientService

    @Shared
    String accessToken

    @Shared
    mx.finerio.pfm.api.domain.Client loggedInClient

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient = clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken

    }

    void cleanup(){
        List<Category> categoriesChild = categoryGormService.findAllByParentIsNotNull()
        categoriesChild.each { Category category ->
            categoryGormService.delete(category.id)
        }

        List<Category> categories = categoryGormService.findAll()
        categories.each { Category category ->
            categoryGormService.delete(category.id)
        }
    }

    private void cleanupData() {
        List<Category> categoriesChild = categoryGormService.findAllByParentIsNotNull()
        categoriesChild.each { Category category ->
            categoryGormService.delete(category.id)
        }

        List<Category> categories = categoryGormService.findAll()
        categories.each { Category category ->
            categoryGormService.delete(category.id)
        }
    }

    def "Should get unauthorized"() {

        given:
        HttpRequest getReq = HttpRequest.GET(CATEGORIES_ROOT)

        when:
        client.toBlocking().exchange(getReq, Map)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.UNAUTHORIZED
    }

    def
    "Should get a categorizer"() {

        given: 'a client'
        String input = 'UBER+EATS'
        HttpRequest getReq = HttpRequest.GET("$CATEGORIES_ROOT/search?input=$input").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        assert rspGET

    }

    def  "Should get a empty list of categories"() {

        given: 'a client'
        HttpRequest getReq = HttpRequest.GET(CATEGORIES_ROOT).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        Map body = rspGET.getBody(Map).get()
        assert !body.isEmpty()
        assert body.get("nextCursor") == null

        List<CategoryDto> categoryDtos= body.get("data") as List<CategoryDto>
        assert categoryDtos.isEmpty()
    }

    def "Should create a category"() {
        given: 'an saved User '
        User user1 = generateUser()

        and: 'a command request body'
        CategoryCreateCommand cmd = generateCategoryCommand(user1)

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd).bearerAuth(accessToken)

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

    def "Should create a category without user"() {

        given: 'a command request body'
        CategoryCreateCommand cmd = new CategoryCreateCommand()
        cmd.with {
            name = 'Shoes and clothes'
            color = "#00FFAA"
        }

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd).bearerAuth(accessToken)

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

    def "Should create a category with parent category"() {
        given: 'an saved User '
        User user1 = generateUser()

        and: 'a command request body'
        CategoryCreateCommand cmd = generateCategoryCommand(user1)

        and:'a saved parent category'
        Category parentCategory =  generateCategory(user1)

        categoryGormService.save(parentCategory)
        cmd.parentCategoryId =  parentCategory.id

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd).bearerAuth(accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, CategoryDto)

        then:
        rsp.status == HttpStatus.OK
        assert rsp.body().with {
            assert cmd
            assert id
            assert dateCreated
            assert lastUpdated
            assert parentCategoryId ==  parentCategory.id
        }

    }

    def "Should throw bad request on create a category with parent category and the parent category is a subcategory"() {
        given: 'an saved User '
        User user1 = generateUser()

        and: 'a command request body'
        CategoryCreateCommand cmd = generateCategoryCommand(user1)

        and: 'a parent category'
        Category parentCategory = new Category()
        parentCategory.name = 'the chosen one'
        parentCategory.client = loggedInClient
        categoryGormService.save(parentCategory)

        and:'a saved pseudo parent category'
        Category pseudoParentCategory =  generateCategory(user1)
        pseudoParentCategory.parent = parentCategory
        categoryGormService.save(pseudoParentCategory)

        cmd.parentCategoryId =  pseudoParentCategory.id


        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorsDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'category.parentCategory.invalid'
            assert title == 'Not a parent category'
            assert detail == 'The parent category you provided is a subcategory. Please provide a parent category.'
        }

    }

    def "Should not create a category and throw bad request on wrong params"() {
        given: 'a category request body with empty body'

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, new CategoryCreateCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, CategoryDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create a transaction and throw bad request on wrong body"() {
        given: 'a transaction request body with empty body'

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, 'asd').bearerAuth(accessToken)

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
        CategoryCreateCommand cmd = generateCategoryCommand(user)

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorsDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'user.notFound'
            assert title == 'User not found.'
            assert detail == 'The user ID you requested was not found.'
        }
    }

    def "Should not create a category and throw not found exception on category not found"() {
        given: 'a saved user'
        User user = generateUser()

        CategoryCreateCommand cmd = generateCategoryCommand(user)
        cmd.parentCategoryId = 666

        HttpRequest request = HttpRequest.POST(CATEGORIES_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorsDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'category.notFound'
            assert title == 'Category not found.'
            assert detail == 'The category ID you requested was not found.'
        }
    }

    def "Should get a category"() {
        given: 'a saved user'
        User user = generateUser()

        and: 'a saved category'
        Category category = generateCategory(user)

        and:
        HttpRequest getReq = HttpRequest.GET(CATEGORIES_ROOT + "/${category.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, CategoryDto)

        then:
        rspGET.status == HttpStatus.OK
        assert rspGET.body().with {
            category
        }
        assert !category.dateDeleted

    }

    def "Should not get a category and throw 404"() {
        given: 'a not found id request'

        HttpRequest request = HttpRequest.GET("${CATEGORIES_ROOT}/0000").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorsDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)

        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'category.notFound'
            assert title == 'Category not found.'
            assert detail == 'The category ID you requested was not found.'
        }

    }

    def "Should not get an account and throw 400"() {
        given: 'a not found id request'

        HttpRequest request = HttpRequest.GET("${CATEGORIES_ROOT}/abc").bearerAuth(accessToken)

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
        Category category = generateCategory(user1)

        and: 'an account command to update data'
        def cmd = generateCategoryCommand(user1)

        and: 'a client'
        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${category.id}", cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>,
                Argument.of(ErrorDto))
        then:
        resp.status == HttpStatus.OK
        resp.body().with {
           cmd
        }

    }

    def "Should partially update an category"() {
        given: 'a saved user'
        User user1 = generateUser()
        User user2 = generateUser()


        and: 'a saved category'
        Category category = generateCategory(user1)
        Category parent = generateCategory(user1)


        and: 'an account command to update data'
        CategoryUpdateCommand cmd = new CategoryUpdateCommand()
        cmd.with {
            userId = user2.id
            name = 'Shoes and clothes changed'
            parentCategoryId = parent.id
        }
        cmd

        and: 'a client'
        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${category.id}", cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>,
                Argument.of(ErrorDto))
        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            assert userId == cmd.userId
            assert name == cmd.name
            assert color == category.color
            assert parentCategoryId == parent.id
        }

    }

    def "Should throw bad request on update a category with parent category and the parent category is a subcategory"() {
        given: 'an saved User '
        User user1 = generateUser()

        and: 'a command request body'
        CategoryCreateCommand cmd = generateCategoryCommand(user1)

        and: 'a parent category'
        Category parentCategory = new Category()
        parentCategory.name = 'the chosen one'
        parentCategory.client = loggedInClient
        categoryGormService.save(parentCategory)

        and:'a saved sub category whit valid parent category'
        Category subCategory =  generateCategory(user1)
        subCategory.parent = parentCategory
        categoryGormService.save(subCategory)

        and:'a saved pseudo parent category that is a subcategory'
        Category pseudoParentCategory =  generateCategory(user1)
        pseudoParentCategory.parent = parentCategory
        categoryGormService.save(pseudoParentCategory)


        and:'a pseudo parent category edit request that is a subcategory'
        cmd.parentCategoryId =  pseudoParentCategory.id


        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${subCategory.id}", cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorsDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'category.parentCategory.invalid'
            assert title == 'Not a parent category'
            assert detail == 'The parent category you provided is a subcategory. Please provide a parent category.'
        }

    }

    def "Should not update an category on not found parent category"() {
        given: 'a saved user'
        User user1 = generateUser()

        and: 'a saved category'
        Category category = generateCategory(user1)

        and: 'an account command to update data'
        def cmd = generateCategoryCommand(user1)
        cmd.parentCategoryId = 100000

        and: 'a client'
        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${category.id}", cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>,
                Argument.of(ErrorDto))
        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND


    }

    def "Should not update a transaction on band parameters and return Bad Request"() {
        given: 'a saved user'
        User user1 = generateUser()

        and: 'a saved category'
        Category category =  generateCategory(user1)


        HttpRequest request = HttpRequest.PUT("${CATEGORIES_ROOT}/${category.id}", []).bearerAuth(accessToken)

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
                generateCategoryCommand(generateUser())).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorsDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'category.notFound'
            assert title == 'Category not found.'
            assert detail == 'The category ID you requested was not found.'
        }
    }

    def "Should get a list of categories with no user set"() {

        given: 'a category list'
        User user1 = generateUser()

        Category category1 =  generateCategory(user1)
        Category category2 = generateCategoryWithoutUser()
        category2.dateDeleted = new Date()
        categoryGormService.save(category2)

        Category category3 =  generateCategoryWithoutUser()
        Category category4 =  generateCategory(user1)
        Category category5 =  generateCategoryWithoutUser()

        and:
        HttpRequest getReq = HttpRequest.GET(CATEGORIES_ROOT).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<CategoryDto> categoryDtos = body.get("data") as List<CategoryDto>
        categoryDtos.size() == 2
        assert !categoryDtos.find {it.id == category1.id}
        assert !categoryDtos.find {it.id == category2.id}
        assert !categoryDtos.find {it.id == category4.id}
        assert categoryDtos.find {it.id == category3.id}
        assert categoryDtos.find {it.id == category5.id}

        assert body.get("nextCursor") == null
    }

    def "Should get a list of categories with a user set"() {

        given: 'a category list'
        User user1 = generateUser()
        User user2 = generateUser()

        Category category1 = generateCategory(user1)
        Category category2 = generateCategory(user1)
        category2.dateDeleted = new Date()
        categoryGormService.save(category2)

        Category category3 =  generateCategory(user1)
        Category category4 =  generateCategory(user2)
        Category category5 = generateCategoryWithoutUser()

        and:
        HttpRequest getReq = HttpRequest.GET("$CATEGORIES_ROOT?userId=${user1.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<CategoryDto> categoryDtos = body.get("data") as List<CategoryDto>

        assert categoryDtos.size() == 3
        assert categoryDtos.find {it.id == category1.id}
        assert !categoryDtos.find {it.id == category2.id}
        assert !categoryDtos.find {it.id == category4.id}
        assert categoryDtos.find {it.id == category3.id}
        assert categoryDtos.find {it.id == category5.id}

        assert body.get("nextCursor") == null
    }

    def "Should get a list of categories in a cursor "() {

        given: 'a category list'
        User user1 = generateUser()

        Category category1 =  generateCategory(user1)

        Category category2 = new Category(generateCategoryCommand(user1), loggedInClient)
        category2.dateDeleted = new Date()
        categoryGormService.save(category2)

        Category category3 = generateCategory(user1)
        Category category4 = generateCategory(user1)

        Category category5 = generateCategory(user1)

        and:
        HttpRequest getReq = HttpRequest.GET("$CATEGORIES_ROOT?cursor=${category4.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<CategoryDto> categoryDtos = body.get("data") as List<CategoryDto>
        assert !(category2.id in categoryDtos.id)
        assert !(category5.id in categoryDtos.id)

        assert body.get("nextCursor") == null
    }

    def "Should throw not found exception on delete no found category"() {
        given:
        def notFoundId = 666

        and: 'a client'
        HttpRequest request = HttpRequest.DELETE("${CATEGORIES_ROOT}/${notFoundId}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>, Argument.of(ErrorsDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'category.notFound'
            assert title == 'Category not found.'
            assert detail == 'The category ID you requested was not found.'
        }

    }

    def "Should delete a category"() {
        given: 'a saved category'
        User user1 = generateUser()

        Category category1 =  generateCategory(user1)

        and: 'a client request'
        HttpRequest request = HttpRequest.DELETE("${CATEGORIES_ROOT}/${category1.id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, CategoryDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("${CATEGORIES_ROOT}/${category1.id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(CategoryDto) as Argument<CategoryDto>,
                Argument.of(ItemNotFoundException))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    private User generateUser() {
        userGormService.save(new User('awesome user', loggedInClient))
    }

    private static CategoryCreateCommand generateCategoryCommand(User user1) {
        CategoryCreateCommand cmd = new CategoryCreateCommand()
        cmd.with {
            userId = user1.id
            name = 'Shoes and clothes'
            color = "#00FFAA"
        }
        cmd
    }

    private Category generateCategory(User user) {
        Category category = new Category(generateCategoryCommand(user), loggedInClient)
        category.user = user
        categoryGormService.save(category)
    }

    private Category generateCategoryWithoutUser() {
        Category category = new Category()
        category.with {
            name = 'ALONE ONE'
            category.client = loggedInClient
        }
        categoryGormService.save(category)
    }

}