package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.AccountDto
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.AccountUpdateCommand
import org.junit.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'account controller')
@MicronautTest(application = Application.class)
class AccountControllerSpec extends Specification {

    public static final String ACCOUNT_ROOT = "/accounts"
    public static final String LOGIN_ROOT = "/login"

    @Shared
    @Inject
    @io.micronaut.http.client.annotation.Client("/")
    RxStreamingHttpClient client

    @Inject
    @Shared
    AccountGormService accountGormService

    @Inject
    @Shared
    TransactionGormService transactionGormService

    @Inject
    UserGormService userGormService

    @Inject
    @Shared
    FinancialEntityGormService financialEntityGormService

    @Inject
    @Shared
    ClientService clientService

    @Shared
    String accessToken

    @Shared
    mx.finerio.pfm.api.domain.Client loggedInClient

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient =clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
    }

    void cleanup(){
        cleanUpData()
    }

    private void cleanUpData() {

        List<Transaction> transactions = transactionGormService.findAll()
        transactions.each {
            transactionGormService.delete(it.id)
        }

        List<Account> accounts = accountGormService.findAll()
        accounts.each { Account account ->
            accountGormService.delete(account.id)
        }
        List<FinancialEntity> entities = financialEntityGormService.findAll()
        entities.each { FinancialEntity entity ->
            financialEntityGormService.delete(entity.id)
        }

    }

    def "Should get unauthorized"() {

        given:
        HttpRequest getReq = HttpRequest.GET(ACCOUNT_ROOT)

        when:
        client.toBlocking().exchange(getReq, Map)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.UNAUTHORIZED
    }

    def "Should get a empty list of accounts"() {

        given:'a user'
        User user = new User('awesome user', loggedInClient)
        userGormService.save(user)

        HttpRequest getReq = HttpRequest.GET("${ACCOUNT_ROOT}?userId=$user.id").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        assert !body.isEmpty()
        List<AccountDto> accountDtos= body.get("data") as List<AccountDto>
        assert accountDtos.isEmpty()
    }

    def "Should get a not found exception on user not found"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET("${ACCOUNT_ROOT}?userId=666").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(getReq,Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
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

    def "Should create an account"(){
        given:'an saved user and financial entity'
        User user =  userGormService.save(new User('awesome user', loggedInClient))

        FinancialEntity entity = generateEntity()

        and:'a command request body'
        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = user.id
            financialEntityId = entity.id
            nature ='DEBIT'
            name = 'awesome account'
            number = 1234123412341234
        }

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT, cmd).bearerAuth(accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, AccountDto)

        then:
        rsp.status == HttpStatus.OK
        rsp.body().with {
            id
            nature == cmd.nature
            name == cmd.name
            number == cmd.number
            balance == cmd.balance
            dateCreated
        }
        assert rsp.body().number instanceof  String

        when:
        Account account = accountGormService.getById(rsp.body().id)

        then:'verify'
        !account.dateDeleted
    }

    def "Should not create an account and throw bad request on wrong params"(){
        given:'an account request body with empty body'

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT,  new AccountCreateCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(ItemNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an account and throw bad request on wrong body"(){
        given:'an account request body with empty body'

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT,  'asd').bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(ItemNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an account and throw not found exception on user not found"(){
        given:'an account request body with no found user id'

        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = 666
            financialEntityId = 666
            nature ='DEBIT'
            name = 'awesome account'
            number = 1234123412341234
            balance = 0.1
        }

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
    }

    def "Should not create an account and throw not found exception on financial entity not found"(){
        given:'an saved user and financial entity'
        User user = generateUser()

        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = user.id
            financialEntityId = 666
            nature ='DEBIT'
            name = 'awesome account'
            number = 1234123412341234
            balance = 0.1
        }

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
    }

    def "Should get an account"(){
        given:'a saved user'
        User user = generateUser()

        and: 'a financial entity'
        FinancialEntity entity = generateEntity()

        and:'a account request command body'
        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId  = user.id
            financialEntityId = entity.id
            nature = 'DEBIT'
            name = 'awesome name'
            number = '1234123412341234'
        }

        and:'a saved account'
        Account account = new Account(cmd, user, entity)
        accountGormService.save(account)

        and:
        HttpRequest getReq = HttpRequest.GET(ACCOUNT_ROOT+"/${account.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, AccountDto)

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().with {
            assert nature == cmd.nature
            assert name == cmd.name
            assert number == cmd.number
            assert balance == cmd.balance
        }
        !account.dateDeleted

    }

    def "Should not get an account and throw 404"(){
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("${ACCOUNT_ROOT}/0000").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'account.notFound'
            assert title == 'Account not found.'
            assert detail == 'The account ID you requested was not found.'
        }

    }

    def "Should not get an account and throw 400"(){
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("${ACCOUNT_ROOT}/abc").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,  Argument.of(AccountDto) as Argument<AccountDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'request.body.invalid'
            assert title == 'Malformed request body'
            assert detail == 'The JSON body request you sent is invalid.'
        }

    }

    def "Should update an account"(){
        given:'a saved user'
        User awesomeUser = generateUser()

        and:'a saved entity'
        FinancialEntity entity1 = generateEntity()

        and:'a saved account'
        Account account = new Account()
        account.with {
            user = awesomeUser
            financialEntity = entity1
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account)

        and:'an account command to update data'
        AccountUpdateCommand cmd = new AccountUpdateCommand()
        cmd.with {
            userId = awesomeUser.id
            financialEntityId = entity1.id
            nature = 'No test'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
            chargeable = false
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${ACCOUNT_ROOT}/${account.id}",  cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, AccountDto)

        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            name == cmd.name
            nature == cmd.nature
            balance == cmd.balance
            chargeable == cmd.chargeable
        }

    }

    def "Should partially update an account"(){
        given:'a saved user'
        User awesomeUser = generateUser()
        User moreAwesomeUser = generateUser()

        and:'a saved entity'
        FinancialEntity entity1 = generateEntity()
        FinancialEntity entity2 = generateEntity()


        and:'a saved account'
        Account account = new Account()
        account.with {
            user = awesomeUser
            financialEntity = entity1
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account)

        and:'an account command to update data'
        AccountUpdateCommand cmd = new AccountUpdateCommand()
        cmd.with {
            userId = moreAwesomeUser.id
            financialEntityId = entity2.id
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${ACCOUNT_ROOT}/${account.id}",  cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, AccountDto)

        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            nature == account.nature
            name == account.name
            number == account.number
            balance == account.balance
        }

    }

    def "Should not update an account on band parameters and return Bad Request"(){
        given:'a saved user'

        HttpRequest request = HttpRequest.PUT("${ACCOUNT_ROOT}/666", []).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,AccountDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update an account and throw not found exception"(){
        given:
        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = 1
            financialEntityId = 123
            nature = 'No test'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${ACCOUNT_ROOT}/${notFoundId}",  cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,  Argument.of(AccountDto) as Argument<AccountDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'account.notFound'
            assert title == 'Account not found.'
            assert detail == 'The account ID you requested was not found.'
        }

    }

    def "Should get a list of accounts"(){

        given:'a saved user'
        User user1 =generateUser()

        and:'a saved entity'
        FinancialEntity entity = generateEntity()

        and:'account list'
        Account account = new Account()
        account.with {
            user = user1
            financialEntity = entity
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        account.dateDeleted = new Date()
        accountGormService.save(account)

        Account account2 = new Account()
        account2.with {
            user = user1
            financialEntity = entity
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account2)

        and:
        HttpRequest getReq = HttpRequest.GET("${ACCOUNT_ROOT}?userId=${user1.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<AccountDto> accounts = body.get("data") as List<AccountDto>
        assert !(account.id in accounts.id)
    }

    def "Should get a list of accounts in a cursor point"(){

        given:'a saved user'
        User user1 =  generateUser()

        and:'a saved entity'
        FinancialEntity entity = generateEntity()

        and:'a list of accounts'
        Account account = new Account()
        account.with {
            user = user1
            financialEntity = entity
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account)

        Account account2 = new Account()
        account2.with {
            user = user1
            financialEntity = entity
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account2)

        Account account3 = new Account()
        account3.with {
            user = user1
            financialEntity = entity
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account3)

        and:
        HttpRequest getReq = HttpRequest.GET("${ACCOUNT_ROOT}?userId=${user1.id}&cursor=${account2.id}")
                .bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<AccountDto> accounts = body.get("data") as List<AccountDto>

        assert accounts.first().id == account2.id
    }

    def "Should throw not found user "(){

        given:'a saved user'
        HttpRequest getReq = HttpRequest.GET("${ACCOUNT_ROOT}?userId=${666}")
                .bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(getReq,  Argument.of(AccountDto) as Argument<AccountDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
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

    def "Should response bad request on user id not send on url"(){

        given:'a saved user'

        HttpRequest getReq = HttpRequest.GET(ACCOUNT_ROOT).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(getReq,  Argument.of(AccountDto) as Argument<AccountDto>,
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

    def "Should throw not found exception on delete no found user"(){
        given:
        HttpRequest request = HttpRequest.DELETE("${ACCOUNT_ROOT}/666").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,  Argument.of(AccountDto) as Argument<AccountDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'account.notFound'
            assert title == 'Account not found.'
            assert detail == 'The account ID you requested was not found.'
        }

    }

    def "Should delete an account"() {
        given:'a saved user'
        User user1 = generateUser()

        and:'a saved entity'
        FinancialEntity entity = generateEntity()

        and:'a saved account'
        Account account = new Account()
        account.with {
            user = user1
            financialEntity = entity
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account)

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${ACCOUNT_ROOT}/${account.id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, UserDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("${ACCOUNT_ROOT}/${account.id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(ItemNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND


    }

    def "Should not delete an account and throw exception on account who has transactions set"() {
        given:'a saved user'
        User user1 = generateUser()

        and:'a saved entity'
        FinancialEntity entity = generateEntity()

        and:'a saved account'
        Account account = new Account()
        account.with {
            user = user1
            financialEntity = entity
            nature = 'test'
            name = 'test'
            number = 1234123412341234
            balance = 0.0
        }
        accountGormService.save(account)

        and: 'a saved transaction how has set the account'
        Transaction transaction = new Transaction()
        transaction.with {
            date = new Date()
            description = 'test description'
            amount = 1000.00
            transaction.account = account
        }
        transactionGormService.save(transaction)

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${ACCOUNT_ROOT}/${account.id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,  Argument.of(AccountDto) as Argument<AccountDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.first().with {
            assert code == 'account.transaction.existence'
            assert title == 'Transaction child existence'
            assert detail == 'There is at least one transaction that is still using this account entity'
        }

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

