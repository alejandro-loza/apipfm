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
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.UserCommand
import org.junit.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'usercontroller')
@MicronautTest(application = Application.class)

class UserControllerSpec extends Specification {

    public static final String LOGIN_ROOT = "/login"
    public static final String USER_ROOT = "/users"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    UserGormService userGormService

    @Inject
    @Shared
    ClientService clientService

    @Shared
    String accessToken

    @Inject
    @Shared
    AccountGormService accountGormService

    @Inject
    @Shared
    FinancialEntityGormService financialEntityGormService

    @Inject
    @Shared
    TransactionGormService transactionGormService


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

        List<Transaction> transactions = transactionGormService.findAll()
        if(!transactions.isEmpty()){
            transactions.each { Transaction transaction ->
                transactionGormService.delete(transaction.id)
            }
        }

        List<Account> accounts = accountGormService.findAll()
        accounts.each { Account account ->
            accountGormService.delete(account.id)
        }

        List<User> users = userGormService.findAll()
        users.each { user ->
            userGormService.delete(user.id)
        }
    }

    def "Should get unauthorized"() {

        given:
        HttpRequest getReq = HttpRequest.GET(USER_ROOT)

        when:
        client.toBlocking().exchange(getReq, Map)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.UNAUTHORIZED
    }

    def "Should get a empty list of users"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET(USER_ROOT).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(Map))

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        assert !body.isEmpty()
        assert body.get("data") == []
        assert body.get("nextCursor") == null
    }

    def "Should create and get user"(){
        given:'an user'
        UserCommand cmd = new UserCommand()
        cmd.with {
            name = 'username'
        }

        HttpRequest request = HttpRequest.POST(USER_ROOT, cmd).bearerAuth(accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, UserDto)

        then:
        rsp.status == HttpStatus.OK
        rsp.body().name == cmd.name
    }

    def 'Should throw exception on an username that already exist'(){


        given:'a saved user'
        userGormService.save(new User('schrodinger', loggedInClient))

        UserCommand cmd = new UserCommand()
        cmd.with {
            name = 'schrodinger'
        }

        HttpRequest request = HttpRequest.POST(USER_ROOT, cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
        e.response.status.code == 400



    }

    def "Should get an user"(){
        given:'a saved user'
        User user =  generateUser()

        and:
        HttpRequest getReq = HttpRequest.GET("${USER_ROOT}/${user.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.of(UserDto) as Argument<UserDto>,
                Argument.of(ErrorDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().name == user.name
        rspGET.body().dateCreated
        rspGET.body().id

    }

    def "Should not create an user an return 400"(){
        given:'an user'
        HttpRequest request = HttpRequest.POST(USER_ROOT,  new UserCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,
                Argument.of(ErrorsDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().errors.size() == 2
    }

    def "Should not create an user with wrong body an return 400"(){
        given:'an user'

        HttpRequest request = HttpRequest.POST(USER_ROOT,  'qwe').bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,
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

    def "Should throw not found exception on no found user"(){
        given:'a not found if'

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.GET("${USER_ROOT}/${notFoundId}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,  Argument.of(ErrorsDto))

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

    def "Should update an user"(){
        given:'a saved user'
        User user =  generateUser()

        and:'an user command to update data'
        UserCommand cmd = new UserCommand()
        cmd.with {
            name = 'awesome name'
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${USER_ROOT}/${user.id}",  cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, UserDto)

        then:
        resp.status == HttpStatus.OK
        resp.body().name == cmd.name

    }

    def 'Should throw exception on an username that already exist on update'(){

        given:'a saved user'
        User user = userGormService.save(new User("schrodinger's cat", loggedInClient))

        UserCommand cmd = new UserCommand()
        cmd.with {
            name = "schrodinger's cat"
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${USER_ROOT}/${user.id}",  cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
        e.response.status.code == 400

    }

    def "Should not update an user on band parameters and return Bad Request"(){
        given:'a saved user'
        User user =  generateUser()

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${USER_ROOT}/${user.id}",  new UserCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request,UserDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update an user and throw not found exception"(){
        given:'an user commando to update data'
        UserCommand cmd = new UserCommand()
        cmd.with {
            name = 'awesome name'
        }

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${USER_ROOT}/${notFoundId}",  cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(User) as Argument<User>, Argument.of(ItemNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of users by a current loggedIn client"(){

        given:'a saved user'
        User user =  generateUser()

        User user2 = new User('no awesome', loggedInClient)
        user2.dateDeleted = new Date()
        userGormService.save(user2)

        and:'an another client user '

        User user3 = new User('another',generateClient())
        userGormService.save(user3)

        and:
        HttpRequest getReq = HttpRequest.GET(USER_ROOT).bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        assert !body.isEmpty()
        assert body.get("data")

        List<UserDto> users= body.get("data") as List<UserDto>
        assert !users.isEmpty()
        assert users.stream().noneMatch{it.id == user2.id}
        assert users.stream().noneMatch{it.id == user3.id}
        assert users.stream().anyMatch{it.id == user.id}

    }

    def "Should get a list of users by a current loggedIn client in a cursor point"(){

        given:'a saved list of users'
        User user = new User('no awesome', loggedInClient)
        user.dateDeleted = new Date()
        userGormService.save(user)

        User user2 = generateUser()
        User user3 = new User('another',generateClient())
        userGormService.save(user3)
        User user4 = generateUser()
        User user5 = generateUser()

        and:
        HttpRequest getReq = HttpRequest.GET("${USER_ROOT}?cursor=${user4.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        assert !body.isEmpty()
        assert body.get("data")
        assert body.get("nextCursor") == null
        List<UserDto> users= body.get("data") as List<UserDto>
        assert !users.isEmpty()
        assert users.stream().noneMatch{it.id == user.id}
        assert users.stream().noneMatch{it.id == user3.id}
        assert users.stream().noneMatch{it.id == user5.id}
        assert users.stream().anyMatch{it.id == user2.id}
        assert users.stream().anyMatch{it.id == user4.id}
        assert users.first().id == user4.id
        assert users.last().id == user2.id
    }

    def "Should get a list of users in a cursor point"() {

        given:'a saved user'
        User user =generateUser()

        User user2 = generateUser()

        User user3 =generateUser()

        and:
        HttpRequest getReq = HttpRequest.GET("${USER_ROOT}?cursor=${user2.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<UserDto> users= body.get("data") as List<UserDto>
        users.first().with {
            assert id == user2.id
            assert name == user2.name
            assert dateCreated
        }
        users.last().id == user.id
    }

    def "Should throw not found exception on delete no found user"(){
        given:
        HttpRequest request = HttpRequest.DELETE("${USER_ROOT}/666").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<User>, Argument.of(ItemNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete an user"() {
        given:'a saved user'
        User user = generateUser()
        Long id = user.id

        and:'a list of accounts'
        Account account1 = generateAccount(user)
        Account account2 = generateAccount(user)

        and:
        generateTransaction(account1)
        generateTransaction(account2)

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${USER_ROOT}/${id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, UserDto)

        then:
        response.status == HttpStatus.NO_CONTENT
        assert userGormService.findById(user.id).dateDeleted

        when:
        def accounts =  accountGormService.findAllByUserAndDateDeletedIsNull(user, [sort: 'id', order: 'desc'])

        then:
        assert accounts.isEmpty()
        when:
        def transactions1 =  transactionGormService.findAllByAccountAndDateDeletedIsNull(account1, [sort: 'id', order: 'desc'])

        then:
        assert transactions1.isEmpty()

        when:
        def transactions2 =  transactionGormService.findAllByAccountAndDateDeletedIsNull(account2, [sort: 'id', order: 'desc'])

        then:
        assert transactions2.isEmpty()

    }

    private Account generateAccount(User user1) {
        Account account = new Account()
        account.with {
            user = user1
            balance = 0.0
            name = 'test'
            number = 'asd'
            nature = 'test'
            financialEntity = generateEntity()
        }
        accountGormService.save(account)
    }

    private  Transaction generateTransaction(Account account1){
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            description = 'test description'
        }
        transactionGormService.save(transaction)
    }

    private User generateUser() {
        userGormService.save(new User('awesome user', loggedInClient))
    }

    private mx.finerio.pfm.api.domain.Client generateClient(){
        clientService.register("another client", 'elementary', ['ROLE_DETECTIVE'])
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
