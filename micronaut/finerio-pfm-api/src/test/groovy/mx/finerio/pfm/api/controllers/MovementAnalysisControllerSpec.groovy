package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.*
import mx.finerio.pfm.api.dtos.testUtils.MovementsAnalysisTestDto
import mx.finerio.pfm.api.dtos.utilities.CategoryAnalysisDto
import mx.finerio.pfm.api.dtos.utilities.MovementsAnalysisDto
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.*
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Property(name = 'spec.name', value = 'movemnt analysis controller')
@MicronautTest(application = Application.class)
class MovementAnalysisControllerSpec extends Specification {

    public static final String LOGIN_ROOT = "/login"
    public static final String ANALYSIS_ROOT = "/analysis"
    public static final boolean EXPENSE = true

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
    @Shared
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

    def setupSpec()
    {
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient = clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken

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

    void cleanup(){
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

    def "Should get a list of transactions  of the account of the user on a range of dates"(){

        given:'a transaction list'
        User user1 = generateUser()
        Account account1 = generateAccount(user1)
        Account account2 = generateAccount(user1)
        Category category1 = generateCategory(user1)
        Category category2 = generateCategory(user1)

        Date sevenMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(7).toInstant())
        Date sixMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(6).plusDays(1).toInstant())
        Date fiveMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(5).toInstant())
        Date oneMonthAgo =  Date.from(ZonedDateTime.now().minusMonths(1).toInstant())
        Date thisMonth =  Date.from(ZonedDateTime.now().toInstant())

        generateTransaction(account2, oneMonthAgo, category2, EXPENSE, 100.00)
        generateTransaction(account1, oneMonthAgo, category1, EXPENSE, 100.00)
        generateTransaction(account1, thisMonth, category1, EXPENSE, 200.00)
        generateTransaction(account2, thisMonth, category2, EXPENSE, 400.00)
        generateTransaction(account2, thisMonth, category2, EXPENSE, 600.00)
        generateTransaction(account1, fiveMonthAgo, category2, EXPENSE, 100.00)
        generateTransaction(account2, fiveMonthAgo, category2, EXPENSE,100.00)

        and:'a 6 months ago transaction'
        generateTransaction(account1, sixMonthAgo, category2, EXPENSE, 100.00)
        generateTransaction(account1, sixMonthAgo, category2, EXPENSE, 100.00)

        and:'a 7 months ago transaction'
        generateTransaction(account1, sevenMonthAgo, category1, EXPENSE, 100.00)
        generateTransaction(account1, sevenMonthAgo, category1, EXPENSE, 100.00)

        and:'a this month deleted one transaction'
        Transaction transaction8 =  generateTransaction(account2, thisMonth, category1, EXPENSE, 100.00)
        transaction8.dateDeleted = new Date()
        transactionGormService.save(transaction8)

        and:
        HttpRequest userRequest = HttpRequest.GET("${ANALYSIS_ROOT}?userId=${user1.id}").bearerAuth(accessToken)

        when:
        def userResponse = client.toBlocking().exchange(userRequest, Argument.of(Map))

        then:
        userResponse.status == HttpStatus.OK
        Map movementsDtos = userResponse.body()
        assert movementsDtos

        List<MovementsAnalysisDto> data = movementsDtos["data"] as  List<MovementsAnalysisDto>
        assert data

        MovementsAnalysisTestDto thisMonthDto =  data.find{it.date == generateFixedDate(thisMonth)}
        assert thisMonthDto
        thisMonthDto.with {
          assert date
          assert categories.size() == 2
        }

        CategoryAnalysisDto thisMonthCategory2 =  thisMonthDto.categories.find{it.categoryId == category2.parent.id } as CategoryAnalysisDto
        assert  thisMonthCategory2
        thisMonthCategory2.with {
            assert amount == 1000
            assert subcategories.size() == 1
            subcategories.first().with {
                assert categoryId
                assert amount == 1000
                assert  transactions.size() == 1
                transactions.first().with {
                    assert amount == 1000
                    assert average == 500
                    assert quantity == 2
                    assert description == 'rapi'
                }
            }
        }

        CategoryAnalysisDto thisMonthCategory1 =  thisMonthDto.categories.find{it.categoryId == category1.parent.id } as CategoryAnalysisDto
        assert  thisMonthCategory1
        thisMonthCategory1.with {
            assert amount == 200
            assert subcategories.size() == 1
            subcategories.first().with {
                assert categoryId
                assert amount
                assert  transactions.size() == 1
                transactions.first().with {
                    assert amount == 200
                    assert average == 200
                    assert quantity == 1
                    assert description == 'rapi'
                }
            }
        }

    }

    private static long generateFixedDate(Date date){
        String rawDate = new SimpleDateFormat("yyyy-MM").format(date)
        generateDate("${rawDate}-01").getTime()
    }

    private static Date generateDate(String rawDate) {
        Date.from(LocalDate.parse(rawDate).atStartOfDay(ZoneId.systemDefault()).toInstant())
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
        userGormService.save(new User('super awesome userr', loggedInClient))
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

    private Transaction generateTransaction(Account accountToSet, Date date1, Category category1, Boolean chargeToSet, BigDecimal amountToSet) {
        Transaction transaction = new Transaction()
        transaction.with {
            account = accountToSet
            charge = chargeToSet
            description = 'rapi'
            amount = amountToSet
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
