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
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.RegisterService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.AccountCommand
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
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    AccountGormService accountGormService

    @Inject
    UserGormService userService

    @Inject
    FinancialEntityGormService financialEntityService

    @Inject
    @Shared
    RegisterService registerService

    @Shared
    String accessToken

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        registerService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
    }

    void setup(){
        List<Account> accounts = accountGormService.findAll()
        accounts.each { Account account ->
            accountGormService.delete(account.id)
        }
    }

    def "Should get a empty list of accounts"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET(ACCOUNT_ROOT).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(AccountDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()
    }

    def "Should create an account"(){
        given:'an saved user and financial entity'
        User user = new User('awesome user')
        user = userService.save(user)

        FinancialEntity entity = new FinancialEntity()
        entity.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        entity = financialEntityService.save(entity)

        and:'a command request body'
        AccountCommand cmd = new AccountCommand()
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
            userId == cmd.userId
            financialEntityId == cmd.financialEntityId
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

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT,  new AccountCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an account and throw bad request on wrong body"(){
        given:'an account request body with empty body'

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT,  'asd').bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an account and throw not found exception on user not found"(){
        given:'an account request body with no found user id'

        AccountCommand cmd = new AccountCommand()
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
        User user = new User('awesome user')
        user = userService.save(user)

        AccountCommand cmd = new AccountCommand()
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
        User user = new User('no awesome name')
        userService.save(user)

        and: 'a financial entity'

        FinancialEntity entity = getSavedFinancialEntity()

        and:'a account request command body'
        AccountCommand cmd = new AccountCommand()
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
            assert userId == cmd.userId
            assert financialEntityId == cmd.financialEntityId
            assert nature == cmd.nature
            assert name == cmd.name
            assert number == cmd.number
            assert balance == cmd.balance
            assert dateCreated == account.dateCreated
            assert lastUpdated == account.lastUpdated
        }
        !account.dateDeleted

    }

    private FinancialEntity getSavedFinancialEntity() {
        FinancialEntity entity = new FinancialEntity()
        entity.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        entity = financialEntityService.save(entity)
        entity
    }

    def "Should not get an account and throw 404"(){
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("/accounts/0000").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should not get an account and throw 400"(){
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("/accounts/abc").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should update an account"(){
        given:'a saved user'
        User awesomeUser = new User('no awesome name')
        awesomeUser = userService.save(awesomeUser)

        and:'a saved entity'
        FinancialEntity entity1 = new FinancialEntity()
        entity1.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        entity1 = financialEntityService.save(entity1)

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
        AccountCommand cmd = new AccountCommand()
        cmd.with {
            userId = awesomeUser.id
            financialEntityId = entity1.id
            nature = 'No test'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("/accounts/${account.id}",  cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, AccountDto)

        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            name == cmd.name
            nature == cmd.nature
            balance == cmd.balance
        }

    }

    def "Should not update an account on band parameters and return Bad Request"(){
        given:'a saved user'

        HttpRequest request = HttpRequest.PUT("/accounts/666",  new AccountCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,AccountDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update an account and throw not found exception"(){
        given:
        AccountCommand cmd = new AccountCommand()
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
        HttpRequest request = HttpRequest.PUT("/accounts/${notFoundId}",  cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(User) as Argument<User>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of accounts"(){

        given:'a saved user'
        User user1 = new User('no awesome')
        userService.save(user1)

        and:'a saved entity'
        FinancialEntity entity = new FinancialEntity()
        entity.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        entity = financialEntityService.save(entity)

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
        HttpRequest getReq = HttpRequest.GET(ACCOUNT_ROOT).bearerAuth(accessToken)

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
        User user1 = new User('no awesome')
        userService.save(user1)

        and:'a saved entity'
        FinancialEntity entity = new FinancialEntity()
        entity.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        entity = financialEntityService.save(entity)

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
        HttpRequest getReq = HttpRequest.GET("/accounts?cursor=${account2.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<AccountDto> accounts = body.get("data") as List<AccountDto>

        assert accounts.first().id == account2.id
    }

    def "Should throw not found exception on delete no found user"(){
        given:
        HttpRequest request = HttpRequest.DELETE("/accounts/666").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete an account"() {
        given:'a saved user'
        User user1 = new User('i will die soon cause i have covid-19')
        userService.save(user1)

        and:'a saved entity'
        FinancialEntity entity = new FinancialEntity()
        entity.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        entity = financialEntityService.save(entity)

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
        HttpRequest request = HttpRequest.DELETE("/accounts/${account.id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, UserDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("/accounts/${account.id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND


    }


}
