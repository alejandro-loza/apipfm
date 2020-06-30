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
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject
import java.time.ZonedDateTime

@Property(name = 'spec.name', value = 'resume controller')
@MicronautTest(application = Application.class)
class ResumeControllerSpec extends Specification{

    public static final String RESUME_ROOT = "/resume"
    public static final String LOGIN_ROOT = "/login"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    AccountGormService accountService

    @Inject
    UserGormService userGormService

    @Inject
    FinancialEntityGormService financialEntityService

    @Inject
    TransactionGormService transactionGormService

    @Inject
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


    def "Should get a list of transactions incomes"(){

        given:'a transaction list'
        User user1 = generateUser()
        Account account1 = generateAccount(user1)

        Account account2 = generateAccount(user1)


        Transaction transaction1 = new Transaction(generateTransactionCommand(account2), account2)
        transactionGormService.save(transaction1)
        Transaction transaction2 = new Transaction(generateTransactionCommand(account1), account1)
        transaction2.dateDeleted = new Date()
        transactionGormService.save(transaction2)
        Transaction transaction3 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction3)
        Transaction transaction4 = new Transaction(generateTransactionCommand(account1), account1)
        transaction4.date =  Date.from(ZonedDateTime.now().minusMonths(7).toInstant())
        transactionGormService.save(transaction4)

        and:
        HttpRequest getReq = HttpRequest.GET("${RESUME_ROOT}/expenses/account/${account1.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(TransactionDto))

        then:
        rspGET.status == HttpStatus.OK
        List<TransactionDto> body = rspGET.body()

        assert body.stream().noneMatch{it.id == transaction2.id}
        assert body.stream().noneMatch{it.id == transaction4.id}
        assert body.stream().anyMatch{it.id == transaction1.id}
        assert body.stream().anyMatch{it.id == transaction3.id}

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
        accountService.save(account1)
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

    private static TransactionCreateCommand generateTransactionCommand(Account account1) {
        def date1 = new Date()

        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = account1.id
            date = date1.getTime()
            charge = true
            description = "UBER EATS"
            amount= 1234.56
        }
        cmd
    }

}
