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
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.BudgetCommand
import mx.finerio.pfm.api.validation.CategoryCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'budget controller')
@MicronautTest(application = Application.class)
class BudgetControllerSpec extends Specification {

    public static final String BUDGETS_ROOT = "/budgets"
    public static final String LOGIN_ROOT = "/login"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    UserGormService userGormService

    @Inject
    CategoryGormService categoryGormService

    @Inject
    BudgetGormService budgetGormService

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
                .bearerAuth(accessToken)
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
    }

    def "Should get a empty list of budgets"() {

        given: 'a client'
        HttpRequest getReq = HttpRequest.GET(BUDGETS_ROOT).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(BudgetDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()
    }

    def "Should create a budget"() {
        given: 'an saved User '
        User user1 = generateUser()
        Category category1 = generateCategory(user1)

        and: 'a command request body'
        BudgetCommand cmd = generateBudgetCommand(user1, category1)

        HttpRequest request = HttpRequest.POST(BUDGETS_ROOT, cmd).bearerAuth(accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, BudgetDto)

        then:
        rsp.status == HttpStatus.OK
        assert rsp.body().with {
            assert cmd
            assert id
            assert dateCreated
            assert lastUpdated
        }

    }

    def "Should not create a budget and throw bad request on wrong params"() {
        given: 'a budget request body with empty body'

        HttpRequest request = HttpRequest.POST(BUDGETS_ROOT, new CategoryCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, CategoryDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create a transaction and throw bad request on wrong body"() {
        given: 'a transaction request body with empty body'

        HttpRequest request = HttpRequest.POST(BUDGETS_ROOT, 'asd').bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, CategoryDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create a budget and throw not found exception on user not found"() {
        given: 'an budget request body with no found account id'

        def user = new User()
        user.id = 666
        Category category = new Category()
        category.id = 666
        BudgetCommand cmd = generateBudgetCommand(user, category)

        HttpRequest request = HttpRequest.POST(BUDGETS_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(BudgetDto) as Argument<BudgetDto>, Argument.of(ErrorDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
    }

    def "Should get a budget"() {
        given: 'a saved user'
        User user = generateUser()

        and: 'a saved category'
        Category category = generateCategory(user)

        and:
        Budget budget = new Budget(generateBudgetCommand(user,category),user,category)
        budgetGormService.save(budget)

        and:
        HttpRequest getReq = HttpRequest.GET(BUDGETS_ROOT + "/${budget.id}").bearerAuth(accessToken)

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

        HttpRequest request = HttpRequest.GET("${BUDGETS_ROOT}/0000").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(ItemNotFoundException))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should not get an account and throw 400"() {
        given: 'a not found id request'

        HttpRequest request = HttpRequest.GET("${BUDGETS_ROOT}/abc").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should update an budget"() {
        given: 'a saved user'
        User user1 = generateUser()

        and: 'a saved category'
        Category category1 = generateCategory(user1)

        and:'a saved budget'
        Budget budget = new Budget(generateBudgetCommand(user1,category1),user1,category1)
        budgetGormService.save(budget)

        and:'a update command'
        BudgetCommand cmd = new BudgetCommand()
        cmd.with {
            userId = user1.id
            categoryId = category1.id
            name = 'changed name'
            amount = 100
            parentBudgetId = 222
        }

        and: 'a client'
        HttpRequest request = HttpRequest.PUT("${BUDGETS_ROOT}/${budget.id}", cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, Argument.of(BudgetDto) as Argument<BudgetDto>,
                Argument.of(ErrorDto))
        then:
        resp.status == HttpStatus.OK
        resp.body().with {
           cmd
        }

    }

    def "Should not update a budget on band parameters and return Bad Request"() {
        given: 'a saved user'
        User user1 = generateUser()

        and: 'a saved category'
        Category category1 = generateCategory(user1)

        and:'a saved budget'
        Budget budget = new Budget(generateBudgetCommand(user1,category1),user1,category1)
        budgetGormService.save(budget)

        HttpRequest request = HttpRequest.PUT("${BUDGETS_ROOT}/${budget.id}", []).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,  Argument.of(BudgetDto) as Argument<BudgetDto>,
                Argument.of(ErrorDto))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update a budget and throw not found exception"() {
        given:
        def notFoundId = 666

        and: 'a client'
        def user = generateUser()
        def category = generateCategory(user)
        HttpRequest request = HttpRequest.PUT("${BUDGETS_ROOT}/${notFoundId}",
                generateBudgetCommand(user, category))
                .bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, BudgetDto)

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of budgets"() {

        given: 'a budget list'
        User user1 = generateUser()
        Category category1 = generateCategory(user1)

        Budget budget2 = generateSavedBudget(user1, category1)
        budget2.dateCreated = new Date()
        budgetGormService.save(budget2)
        3.times {
            generateSavedBudget(user1, category1)
        }

        and:
        HttpRequest getReq = HttpRequest.GET(BUDGETS_ROOT).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<BudgetDto> budgetDtos = body.get("data") as List<BudgetDto>
        assert !(budget2.id in budgetDtos.id)

        assert !body.get("nextCursor")
    }


    def "Should get a list of budgets in a cursor "() {

        given: 'a budget list'
        User user1 = generateUser()
        Category category1 = generateCategory(user1)

        Budget budget1 = generateSavedBudget(user1, category1)
        budget1.dateCreated = new Date()
        budgetGormService.save(budget1)
        Budget budget2 = generateSavedBudget(user1, category1)
        budgetGormService.save(budget2)
        Budget budget3 = generateSavedBudget(user1, category1)
        budgetGormService.save(budget3)
        Budget budget4 = generateSavedBudget(user1, category1)
        budgetGormService.save(budget4)

        and:
        HttpRequest getReq = HttpRequest.GET("$BUDGETS_ROOT?cursor=${budget3.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<BudgetDto> budgetDtos = body.get("data") as List<BudgetDto>
        assert !(budget1.id in budgetDtos.id)
        assert !(budget4.id in budgetDtos.id)

        assert !body.get("nextCursor")
    }


    def "Should throw not found exception on delete no found budget"() {
        given:
        def notFoundId = 666

        and: 'a client'
        HttpRequest request = HttpRequest.DELETE("${BUDGETS_ROOT}/${notFoundId}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,
                Argument.of(BudgetDto) as Argument<BudgetDto>,
                Argument.of(ItemNotFoundException))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete a budget"() {
        given: 'a saved budget'
        User user1 = generateUser()
        Category category = generateCategory(user1)

        and:' a saved budget'
        Budget budget = generateSavedBudget(user1,category)

        and: 'a client request'
        HttpRequest request = HttpRequest.DELETE("${BUDGETS_ROOT}/${budget.id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, BudgetDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("${BUDGETS_ROOT}/${budget.id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(BudgetDto) as Argument<BudgetDto>,
                Argument.of(ItemNotFoundException))

        then:
        def e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    private User generateUser() {
        userGormService.save(new User('awesome user', loggedInClient))
    }

    private Category generateCategory(User user1) {
        Category category1 = new Category()
        category1.with {
            user = user1
            name = 'Shoes and clothes'
            color = "#00FFAA"
            category1.client = loggedInClient
        }
        categoryGormService.save(category1)
        category1
    }

    private static BudgetCommand generateBudgetCommand(User user, Category category) {
        BudgetCommand cmd = new BudgetCommand()
        cmd.with {
            userId = user.id
            categoryId = category.id
            name = "Food budget"
            amount = 1234.56
            parentBudgetId = 123
        }
        cmd
    }

    private Budget generateSavedBudget(User user1, Category category1) {
        Budget budget1 = new Budget(generateBudgetCommand(user1, category1),user1,category1)
        budgetGormService.save(budget1)
        budget1
    }

}