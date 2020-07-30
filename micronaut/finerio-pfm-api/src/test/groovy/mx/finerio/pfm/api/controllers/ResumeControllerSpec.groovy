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
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.dtos.utilities.ResumeDto
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.time.ZonedDateTime

@Property(name = 'spec.name', value = 'resume controller')
@MicronautTest(application = Application.class)
class ResumeControllerSpec extends Specification{

    public static final String RESUME_ROOT = "/resume"
    public static final String LOGIN_ROOT = "/login"
    public static final boolean EXPENSE = true
    public static final boolean INCOME = false

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    @Shared
    AccountGormService accountGormService

    @Inject
    UserGormService userGormService

    @Inject
    FinancialEntityGormService financialEntityService

    @Inject
    TransactionGormService transactionGormService

    @Inject
    @Shared
    CategoryGormService categoryGormService

    @Inject
    @Shared
    ClientService clientService

    @Shared
    mx.finerio.pfm.api.domain.Client loggedInClient

    @Shared
    String accessToken

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient = clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
    }

    void setup(){
        List<Transaction> transactions = transactionGormService.findAll()
        transactions.each {
            transactionGormService.delete(it.id)
        }

        List<Category> categoriesChild = categoryGormService.findAllByParentIsNotNull()
        categoriesChild.each { Category category ->
            categoryGormService.delete(category.id)
        }

        List<Category> categories = categoryGormService.findAll()
        categories.each { Category category ->
            categoryGormService.delete(category.id)
        }
        List<Account> accounts = accountGormService.findAll()
        accounts.each { Account account ->
            accountGormService.delete(account.id)
        }
    }

    def "Should get unauthorized"() {

        given:
        HttpRequest getReq = HttpRequest.GET(RESUME_ROOT)

        when:
        client.toBlocking().exchange(getReq,  Argument.of(ResumeDto) as Argument<ResumeDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.UNAUTHORIZED
    }

    def "Should get bad request"() {

        given:
        HttpRequest getReq = HttpRequest.GET(RESUME_ROOT).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(getReq, Argument.of(ResumeDto) as Argument<ResumeDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'url.query.value.invalid'
            assert title == 'A query parameter in the URL is invalid'
            assert detail == 'A URL query parameter you provided is invalid. Please review it'
        }
    }

    def "Should get a list of transactions incomes of the accounts of the user"(){

        given:'a transaction list'
        User user1 = generateUser()
        Account account1 = generateAccount(user1)
        Account account2 = generateAccount(user1)
        Category category1 = generateCategory(user1)
        Category category2 = generateCategory(user1)


        Date sevenMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(7).toInstant())
        Date sixMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fiveMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(5).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())

        Transaction transaction1 = generateTransaction(account2, oneMonthAgo, category2, EXPENSE)
        Transaction transaction2 = generateTransaction(account1, oneMonthAgo, category1, INCOME)
        Transaction transaction3 = generateTransaction(account1, thisMonth, category2, EXPENSE)
        Transaction transaction4 = generateTransaction(account2, thisMonth, category2, INCOME)
                                   generateTransaction(account1, fiveMonthAgo, category2, EXPENSE)
                                   generateTransaction(account2, fiveMonthAgo, category2, INCOME)

              Transaction transaction5 = generateTransaction(account1, sixMonthAgo, category2, EXPENSE)
              Transaction transaction6 = generateTransaction(account1, sixMonthAgo, category2, INCOME)

        and:'a 7 months ago transaction'
        Transaction sevenMonthsAgoTransactionExpense =  generateTransaction(account1, sevenMonthAgo, category1, EXPENSE)
        Transaction sevenMonthsAgoTransactionIncome =  generateTransaction(account1, sevenMonthAgo, category1, INCOME)


        and:'a this month deleted one transaction'
        Transaction transaction8 =  generateTransaction(account2, thisMonth, category1, INCOME)
        transaction8.dateDeleted = new Date()
        transactionGormService.save(transaction8)

        and:
        HttpRequest getReq = HttpRequest.GET("${RESUME_ROOT}?userId=${user1.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.of(ResumeDto))

        then:
        rspGET.status == HttpStatus.OK
        ResumeDto body = rspGET.body()

        assert body.expenses.size() == 3
        assert body.incomes.size() == 3
        assert body.balances.size() == 3

        assert  body.balances.last().date == body.incomes.last().date
        assert  body.balances.first().date == body.expenses.first().date
        assert  body.balances*.incomes
        assert  body.balances*.expenses

    }

    def "Should get a list of transactions incomes of the accounts of the user issue test"(){

        given:'a transaction list'
        User user1 = generateUser()
        Account account1 = generateAccount(user1)
        Account account2 = generateAccount(user1)
        Category category1 = generateCategory(user1)
        Category category2 = generateCategory(user1)

        and:'a list of months ranges to test'
        Date sixMonthshAgo =  Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())


        Transaction transaction1 = generateTransaction(account2, oneMonthAgo, category2, EXPENSE)
        Transaction transaction2 = generateTransaction(account2, oneMonthAgo, category1, INCOME)
        Transaction transaction3 = generateTransaction(account2, thisMonth, category2, EXPENSE)
        Transaction transaction4 = generateTransaction(account2, thisMonth, category2, INCOME)

        and:'a 7 months ago transaction'
        Transaction sevenMonthsAgoTransactionExpense =  generateTransaction(account1, sixMonthshAgo, category1, EXPENSE)
        Transaction sevenMonthsAgoTransactionIncome =  generateTransaction(account1, sixMonthshAgo, category1, INCOME)

        and:'a this month deleted one transaction'
        Transaction transaction8 =  generateTransaction(account2, thisMonth, category1, INCOME)
        transaction8.dateDeleted = new Date()
        transactionGormService.save(transaction8)

        and:
        HttpRequest getReq = HttpRequest.GET("${RESUME_ROOT}?userId=${user1.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.of(ResumeDto))

        then:
        rspGET.status == HttpStatus.OK
        ResumeDto body = rspGET.body()

        assert body.expenses.size() == 2
        assert body.incomes.size() == 2
        assert body.balances.size() == 2

        def dates = body.balances*.date. collect{
            new Date(it)
        }

        and:'assert the month group on balances dates is actually the two valid ones'

        assert dates.size() == 2
        assert dates.find{ it.month == thisMonth.month && it.year == thisMonth.year}
        assert dates.find{ it.month == oneMonthAgo.month && it.year == oneMonthAgo.year}
        assert !dates.find{ it.month == sixMonthshAgo.month && it.year == sixMonthshAgo.year}


        assert  body.balances.last().date == body.incomes.last().date
        assert  body.balances.first().date == body.expenses.first().date

        assert  body.balances*.incomes
        assert  body.balances*.expenses

    }

    private Account generateAccount(User user1) {
        FinancialEntity entity = generateEntity()

        Account account1 = new Account()
        account1.with {
            user = user1
            financialEntity = entity
            nature = 'TEST NATURE'
            name = 'TEST NAME'
            number = 123412341234
            balance = 0.0
        }
        accountGormService.save(account1)
        account1
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
        financialEntityService.save(entity1)
    }

    private Transaction generateTransaction(Account accountToSet, Date date1, Category category1, Boolean chargeToSet) {
        Transaction transaction = new Transaction()
        transaction.with {
            account = accountToSet
            charge = chargeToSet
            description = 'rapi'
            amount = 100.00
            date = date1
            category = category1
        }
        transactionGormService.save(transaction)
    }

    private  Category generateCategory(User user1) {
        Category parentCat = new Category()
        parentCat.with {
            user = user1
            name = 'test parent category'
            color = 'test parent  color'
            parentCat.client = loggedInClient
        }

        categoryGormService.save(parentCat)

        Category category = new Category()
        category.with {
            user = user1
            name = 'test category'
            color = 'test color'
            category.client = loggedInClient
            parent = parentCat
        }
        categoryGormService.save(category)
    }

}
