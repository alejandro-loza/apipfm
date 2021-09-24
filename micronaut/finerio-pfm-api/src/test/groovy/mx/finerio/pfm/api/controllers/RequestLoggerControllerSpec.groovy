package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.RequestLogger
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.dtos.utilities.RequestLoggerDto
import mx.finerio.pfm.api.enums.EventType
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.RequestLoggerGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.time.ZonedDateTime

@Property(name = 'spec.name', value = 'request logger controller')
@MicronautTest(application = Application.class)
class RequestLoggerControllerSpec extends Specification{
    public static final String LOGGER_ROOT = '/requestLogger'
    public static final String LOGIN_ROOT = "/login"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    @Shared
    ClientService clientService


    @Shared
    mx.finerio.pfm.api.domain.Client loggedInClient

    @Inject
    @Shared
    AccountGormService accountGormService

    @Inject
    UserGormService userGormService

    @Inject
    @Shared
    FinancialEntityGormService financialEntityGormService

    @Inject
    @Shared
    RequestLoggerGormService requestLoggerGormService

    @Inject
    @Shared
    TransactionGormService transactionGormService

    @Inject
    @Shared
    CategoryGormService categoryGormService

    @Shared
    String accessToken

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient = clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
    }

    void cleanup() {

        List<Transaction> transactions = transactionGormService.findAll()
        if (!transactions.isEmpty()) {
            transactions.each { Transaction transaction ->
                transactionGormService.delete(transaction.id)
            }
        }

        List<Account> accounts = accountGormService.findAll()
        accounts.each { Account account ->
            accountGormService.delete(account.id)
        }

        List<Category> categoriesChild = categoryGormService.findAllByParentIsNotNull()
        categoriesChild.each { Category category ->
            categoryGormService.delete(category.id)
        }

        List<Category> categories = categoryGormService.findAll()
        categories.each { Category category ->
            categoryGormService.delete(category.id)
        }

        List<RequestLogger> logs = requestLoggerGormService.findAll()
        logs.each { log ->
            requestLoggerGormService.delete(log.id)
        }

        List<User> users = userGormService.findAll()
        users.each { user ->
            userGormService.delete(user.id)
        }
    }

    def "Should get all request logs"(){
        given:
        def user1 = generateUser()
        def user2 = generateUser()

        Date sevenMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(7).toInstant())
        Date sixMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fiveMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(5).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())

        21.times {
            generateRequestLogger(user1,EventType.USER_CREATE, thisMonth)
            generateRequestLogger(user1,EventType.ACCOUNT_UPDATE, oneMonthAgo)
            generateRequestLogger(user2,EventType.TRANSACTION_DELETE, fiveMonthAgo)
            generateRequestLogger(user2,EventType.USER_GET, sixMonthAgo)
            generateRequestLogger(user2,EventType.ACCOUNT_LIST, sevenMonthAgo)
        }


        and:
        HttpRequest getReq = HttpRequest.GET("${LOGGER_ROOT}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        def body = rspGET.body()

        assert  body.data.size() == 100

        assert  body.nextCursor

    }

    def "Should get request logs by cursor"(){
        given:
        def user1 = generateUser()
        def user2 = generateUser()

        Date sevenMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(7).toInstant())
        Date sixMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fiveMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(5).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())

        def rl1 = generateRequestLogger(user1,EventType.USER_CREATE, thisMonth)
        def rl2 = generateRequestLogger(user1,EventType.ACCOUNT_UPDATE, oneMonthAgo)
        def rl3 = generateRequestLogger(user2,EventType.TRANSACTION_DELETE, fiveMonthAgo)
        def rlc = generateRequestLogger(user2,EventType.USER_GET, sixMonthAgo)
        def rll = generateRequestLogger(user2,EventType.ACCOUNT_LIST, sevenMonthAgo)

        and:
        HttpRequest getReq = HttpRequest.GET("${LOGGER_ROOT}?cursor=${rlc.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        def body = rspGET.body()

        List<RequestLoggerDto> logs = body.data as List<RequestLoggerDto>

        assert logs.find{it.id == rl1.id}
        assert logs.find{it.id == rl2.id}
        assert logs.find{it.id == rl3.id}
        assert logs.find{it.id == rlc.id}
        assert !logs.find{it.id == rll.id}


    }

    def "Should get all request logs filter by type"(){
        given:
        def user1 = generateUser()
        def user2 = generateUser()

        Date sevenMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(7).toInstant())
        Date sixMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fiveMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(5).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())

        def rl = generateRequestLogger(user1,EventType.USER_CREATE, thisMonth)
        generateRequestLogger(user1,EventType.ACCOUNT_UPDATE, oneMonthAgo)
        generateRequestLogger(user2,EventType.TRANSACTION_DELETE, fiveMonthAgo)
        generateRequestLogger(user2,EventType.USER_GET, sixMonthAgo)
        generateRequestLogger(user2,EventType.ACCOUNT_LIST, sevenMonthAgo)


        and:
        HttpRequest getReq = HttpRequest.GET("${LOGGER_ROOT}?eventType=${EventType.USER_CREATE}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        def body = rspGET.body()
        List<RequestLoggerDto> logs = body.data as List<RequestLoggerDto>

        assert logs.size() == 1
        assert logs.find{it.id == rl.id}

    }

    def "Should get all request logs filter by user"(){
        given:
        def user1 = generateUser()
        def user2 = generateUser()

        Date sevenMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(7).toInstant())
        Date sixMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fiveMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(5).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())

        generateRequestLogger(user1,EventType.USER_CREATE, thisMonth)
        generateRequestLogger(user1,EventType.ACCOUNT_UPDATE, oneMonthAgo)
        generateRequestLogger(user2,EventType.TRANSACTION_DELETE, fiveMonthAgo)
        generateRequestLogger(user2,EventType.USER_GET, sixMonthAgo)
        generateRequestLogger(user2,EventType.ACCOUNT_LIST, sevenMonthAgo)


        and:
        HttpRequest getReq = HttpRequest.GET("${LOGGER_ROOT}?userId=${user2.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        def body = rspGET.body()
        List<RequestLoggerDto> logs = body.data as List<RequestLoggerDto>

        assert logs
        assert logs.size() == 3
        assert logs.every{it.userId == user2.id}

    }

    def "Should get all request logs filter by user and dates"(){
        given:
        def user1 = generateUser()
        def user2 = generateUser()

        Date sixMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fiveMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(5).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())

        def rl1 = generateRequestLogger(user1,EventType.USER_CREATE, thisMonth)
        def rl2 = generateRequestLogger(user1,EventType.ACCOUNT_UPDATE, oneMonthAgo)
        def rl3 = generateRequestLogger(user1,EventType.TRANSACTION_DELETE, fiveMonthAgo)
        def rl4 = generateRequestLogger(user1,EventType.USER_GET, sixMonthAgo)
        def rl5 = generateRequestLogger(user2,EventType.ACCOUNT_LIST, oneMonthAgo)

        and:
        HttpRequest getReq = HttpRequest.GET("${LOGGER_ROOT}?userId=${user1.id}" +
                "&dateFrom=${fiveMonthAgo.getTime()}" +
                "&dateTo=${oneMonthAgo.getTime()}"
        ).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        def body = rspGET.body()
        List<RequestLoggerDto> logs = body.data as List<RequestLoggerDto>

        assert logs
        assert !logs.find{it.id == rl1.id}
        assert logs.find{it.id == rl2.id}
        assert logs.find{it.id == rl3.id}
        assert !logs.find{it.id == rl4.id}
        assert !logs.find{it.id == rl5.id}
        assert logs.size() == 2


    }



    private RequestLogger generateRequestLogger(User user1, EventType type, Date creationDate) {
        RequestLogger requestLogger = new RequestLogger()
        requestLogger.with {
            user = user1
            eventType = type
        }
        requestLoggerGormService.save(requestLogger)
        requestLogger.dateCreated = creationDate
        requestLoggerGormService.save(requestLogger)

    }

    private Account generateAccount(User user1) {
        Account account = new Account()
        account.with {
            user = user1
            balance = 0.0
            name = 'test'
            cardNumber = 'asd'
            nature = 'test'
            financialEntity = generateEntity()
        }
        accountGormService.save(account)
    }

    private  Transaction generateTransaction(Account account1){
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            executionDate = new Date()
            description = 'test description'
        }
        transactionGormService.save(transaction)
    }

    private User generateUser() {
        userGormService.save(new User('awesome user', loggedInClient))
    }


    private FinancialEntity generateEntity() {
        FinancialEntity entity1 = new FinancialEntity()
        entity1.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
            entity1.client = loggedInClient
        }
        financialEntityGormService.save(entity1)
    }



}
