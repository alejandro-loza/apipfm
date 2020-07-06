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
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.CategoryGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import javax.inject.Inject

@Property(name = 'spec.name', value = 'account controller')
@MicronautTest(application = Application.class)
class TransactionControllerSpec extends Specification {

    public static final String TRANSACTION_ROOT = "/transactions"
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

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient = clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
        List<Category> categories = categoryGormService.findAll()
        categories.each { Category category ->
            categoryGormService.delete(category.id)
        }

    }

    def "Should get a empty list of transactions"(){

        given:'a client'
        Account account1 = generateAccount()

        HttpRequest getReq = HttpRequest.GET("${TRANSACTION_ROOT}?accountId=${account1.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        assert !body.isEmpty()
        assert body.get("data") ==[]
        assert body.get("nextCursor") == null

        List<TransactionDto> transactionDtos= body.get("data") as List<TransactionDto>
        assert transactionDtos.isEmpty()
    }

    def "Should create a transaction"(){
        given:'an saved Account '
        Account account1 = generateAccount()
        def user = generateUser()

        Category category1 = generateCategory(user)
        category1.parent = generateCategory(user)
        categoryGormService.save(category1)

        and:'a command request body'
        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = account1.id
            date = 1587567125458
            charge = true
            description = "UBER EATS"
            amount= 1234.56
            categoryId = category1.id
        }


        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT, cmd).bearerAuth(accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, TransactionDto)

        then:
        rsp.status == HttpStatus.OK
        rsp.body.get().accountId == account1.id
        rsp.body.get().categoryId == category1.id
    }

    def "Should not create a transaction and throw bad request on wrong params"(){
        given:'an transaction request body with empty body'

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT,  new TransactionCreateCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create a transaction and throw bad request on wrong body"(){
        given:'a transaction request body with empty body'

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT,  'asd').bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an transaction and throw not found exception on account not found"(){
        given:'an account request body with no found account id'

        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 666
            date = 1587567125458
            charge = true
            description = 'UBER EATS'
            amount = 100.00
            categoryId = 123
        }

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
    }

    def "Should not create an transaction and throw not found exception on category not found"(){

        given:'an transaction request body with no found category id'
        Account account1 = generateAccount()
        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = account1.id
            date = 1587567125458
            charge = true
            description = 'UBER EATS'
            amount = 100.00
            categoryId = 666
        }

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
    }

    def "Should get an transaction"(){
        given:'a saved account'
        Account account1 = generateAccount()

        and:'a saved transaction'
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            charge = false
            description = 'RAPI'
        }

        transactionGormService.save(transaction)

        and:
        HttpRequest getReq = HttpRequest.GET(TRANSACTION_ROOT+"/${transaction.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, TransactionDto)

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().with {
            assert id == transaction.id
            assert accountId == transaction.account.id
            assert date == transaction.date
            assert charge == transaction.charge
            assert description == transaction.description
            assert amount == transaction.amount
        }
        !transaction.dateDeleted

    }

    def "Should not get a transaction and throw 404"(){
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("${TRANSACTION_ROOT}/0000").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should not get an account and throw 400"(){
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("${TRANSACTION_ROOT}/abc").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should update an transaction"(){
        given:'a saved user'
        Account account1 = generateAccount()

        and:'a saved transaction'
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            charge = true
            description = 'rapi'
            amount = 100.00
        }
        transactionGormService.save(transaction)

        and:'an account command to update data'
        TransactionCreateCommand cmd = generateTransactionCommand(account1)

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${TRANSACTION_ROOT}/${transaction.id}",  cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request,  Argument.of(TransactionDto) as Argument<TransactionDto>,
                Argument.of(ErrorDto))
        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            assert accountId == cmd.accountId
            assert date.getTime() == cmd.date
            assert charge == cmd.charge
            assert description == cmd.description
            assert amount == cmd.amount
        }

    }

    def "Should not update a transaction on band parameters and return Bad Request"(){
        given:'a transaction '
        Account account1 = generateAccount()

        and:'a saved transaction'
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            charge = true
            description = 'rapi'
            amount = 100.00
        }
        transactionGormService.save(transaction)

        HttpRequest request = HttpRequest.PUT("${TRANSACTION_ROOT}/${transaction.id}", [])
                .bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should partially update an transaction"(){
        given:'a saved user'
        Account account1 = generateAccount()

        and:'a saved transaction'
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            charge = true
            description = 'rapi'
            amount = 100.00
        }
        transactionGormService.save(transaction)

        and:'an account command to update data'
        TransactionUpdateCommand cmd = new TransactionUpdateCommand()
        cmd.with {
            charge = false
            description = "Rappi"
            amount= 2234.56
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${TRANSACTION_ROOT}/${transaction.id}",  cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request,  Argument.of(TransactionDto) as Argument<TransactionDto>,
                Argument.of(ErrorDto))
        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            assert accountId == transaction.account.id
            assert date.toString() == transaction.date.toString()
            assert description == cmd.description
            assert amount == cmd.amount
            assert charge == cmd.charge
        }

    }

    def "Should not update an transaction and throw not found exception"(){
        given:
        Account account1 = generateAccount()

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${TRANSACTION_ROOT}/666",  generateTransactionCommand(account1))
                .bearerAuth(accessToken)
        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of transactions by account"(){

        given:'a transaction list'
        Account account1 = generateAccount()

        Category category = generateCategory(account1.user)

        Transaction transaction1 = new Transaction(generateTransactionCommand(account1), account1, category)
        transactionGormService.save(transaction1)
        Transaction transaction2 = new Transaction(generateTransactionCommand(account1), account1, category)
        transaction2.dateDeleted = new Date()
        transactionGormService.save(transaction2)
        Transaction transaction3 = new Transaction(generateTransactionCommand(account1), account1, category)
        transactionGormService.save(transaction3)
        Transaction transaction4 = new Transaction(generateTransactionCommand(account1), account1, category)
        transactionGormService.save(transaction4)

        and:
        HttpRequest getReq = HttpRequest.GET("${TRANSACTION_ROOT}?accountId=${account1.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<TransactionDto> transactionDtos = body.get("data") as List<TransactionDto>
        assert !(transaction2.id in transactionDtos.id)

        assert body.get("nextCursor")
    }

    def "Should get a list of transactions of an account on a cursor point"(){

        given:'a transaction list'
        Account account1 = generateAccount()
        Account account2 = generateAccount()

        Category category = generateCategory(account1.user)


        Transaction transaction1 = new Transaction(generateTransactionCommand(account2), account2, category)
        transactionGormService.save(transaction1)
        Transaction transaction2 = new Transaction(generateTransactionCommand(account1), account1, category)
        transaction2.dateDeleted = new Date()
        transactionGormService.save(transaction2)
        Transaction transaction3 = new Transaction(generateTransactionCommand(account1), account1, category)
        transactionGormService.save(transaction3)
        Transaction transaction4 = new Transaction(generateTransactionCommand(account1), account1, category)
        transactionGormService.save(transaction4)

        and:
        HttpRequest getReq = HttpRequest.GET("${TRANSACTION_ROOT}?accountId=${account1.id}&cursor=${transaction3.id}")
                .bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<TransactionDto> transactionDtos = body.get("data") as List<TransactionDto>
        assert !(transaction1.id in transactionDtos.id)
        assert !(transaction2.id in transactionDtos.id)
        assert !(transaction4.id in transactionDtos.id)
        transactionDtos.size() == 1
    }


    def "Should throw not found exception on delete no found transaction"(){
        given:
        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.DELETE("${TRANSACTION_ROOT}/${notFoundId}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(ItemNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete an transaction"() {
        given:'a transaction'
        Account account1 = generateAccount()

        Category category = generateCategory(account1.user)


        Transaction transaction1 = new Transaction(generateTransactionCommand(account1), account1, category)
        transactionGormService.save(transaction1)

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${TRANSACTION_ROOT}/${transaction1.id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, TransactionDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("${TRANSACTION_ROOT}/${transaction1.id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>,
                Argument.of(ItemNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

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

    private Account generateAccount() {
        User user1 = generateUser()

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

    private Category generateCategory(User userToSet){
        Category category = new Category()
        category.with {
            user =userToSet
            name = 'category test'
            color = '#12312'
            category.client = loggedInClient
        }
        categoryGormService.save(category)
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

}
