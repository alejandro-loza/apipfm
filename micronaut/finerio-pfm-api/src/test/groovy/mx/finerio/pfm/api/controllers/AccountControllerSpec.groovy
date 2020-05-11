package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.exceptions.UserNotFoundException
import mx.finerio.pfm.api.services.gorm.AccountService
import mx.finerio.pfm.api.services.gorm.UserServiceRepository
import mx.finerio.pfm.api.validation.AccountCommand
import mx.finerio.pfm.api.validation.UserCreateCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'usercontroller')
@MicronautTest(application = Application.class)
class AccountControllerSpec extends Specification {

    public static final String ACCOUNT_ROOT = "/accounts"
    public static final String USER_NOT_FOUND_MESSAGE = 'The user ID you requested was not found.'

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    AccountService accountService

    @Inject
    UserServiceRepository userService

    def "Should get a empty list of accounts"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET(ACCOUNT_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(AccountDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()
    }

    def "Should create an account"(){
        given:'an account request body'
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

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT, cmd)

        when:
        def rsp = client.toBlocking().exchange(request, AccountDto)

        then:
        rsp.status == HttpStatus.OK
        rsp.body().with {
            id
            user.id == cmd.userId
            financialEntityId == cmd.financialEntityId
            nature == cmd.nature
            name == cmd.name
            number == cmd.number
            balance == cmd.balance
            dateCreated
        }

        when:
        Account account = accountService.getById(rsp.body().id)

        then:'verify'
        !account.dateDeleted
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

        HttpRequest request = HttpRequest.POST(ACCOUNT_ROOT, cmd)

        when:
        client.toBlocking().exchange(request, Argument.of(AccountDto) as Argument<AccountDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<UserNotFoundException> jsonError = e.response.getBody(NotFoundException)

        then:
        jsonError.isPresent()
        jsonError.get().message == USER_NOT_FOUND_MESSAGE
    }

    def "Should get an user"(){
        given:'a saved user'
        User user = new User('no awesome name')
        accountService.save(user)

        and:
        HttpRequest getReq = HttpRequest.GET("/users/${user.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, UserDto)

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().name == user.name
        rspGET.body().dateCreated
        rspGET.body().id

    }

    def "Should not create an user an return 400"(){
        given:'an user'

        HttpRequest request = HttpRequest.POST('/users',  new UserCreateCommand())

        when:
        client.toBlocking().exchange(request,UserDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should throw not found exception on no found user"(){
        given:'a not found if'

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.GET("/users/${notFoundId}")

        when:
        client.toBlocking().exchange(request, Argument.of(User) as Argument<User>, Argument.of(UserNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<UserNotFoundException> jsonError = e.response.getBody(UserNotFoundException)

        then:
        jsonError.isPresent()
        jsonError.get().message == USER_NOT_FOUND_MESSAGE

    }

    def "Should update an user"(){
        given:'a saved user'
        User user = new User('no awesome name')
        accountService.save(user)

        and:'an user command to update data'
        UserCreateCommand cmd = new UserCreateCommand()
        cmd.with {
            name = 'awesome name'
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("/users/${user.id}",  cmd)

        when:
        def resp = client.toBlocking().exchange(request, UserDto)

        then:
        resp.status == HttpStatus.OK
        resp.body().name == cmd.name

    }

    def "Should not update an user on band parameters and return Bad Request"(){
        given:'a saved user'
        User user = new User('no awesome name')
        accountService.save(user)

        and:'a client'
        HttpRequest request = HttpRequest.PUT("/users/${user.id}",  new UserCreateCommand())

        when:
        client.toBlocking().exchange(request,UserDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update an user and throw not found exception"(){
        given:'an user commando to update data'
        UserCreateCommand cmd = new UserCreateCommand()
        cmd.with {
            name = 'awesome name'
        }

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.PUT("/users/${notFoundId}",  cmd)

        when:
        client.toBlocking().exchange(request, Argument.of(User) as Argument<User>, Argument.of(UserNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<UserNotFoundException> jsonError = e.response.getBody(UserNotFoundException)

        then:
        jsonError.isPresent()
        jsonError.get().message == USER_NOT_FOUND_MESSAGE

    }

    def "Should get a list of users"(){

         given:'a saved user'
         User user = new User('no awesome')
         accountService.save(user)

         User user2 = new User('awesome')
         accountService.save(user2)
        and:
        HttpRequest getReq = HttpRequest.GET(ACCOUNT_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<UserDto> users= body.get("users") as List<UserDto>
        assert users.size() > 0
        assert body.get("nextCursor") == users.last().id
    }

    def "Should get a list of users in a offset point"(){

        given:'a saved user'
        User user = new User('no awesome')
        accountService.save(user)

        User user2 = new User('awesome')
        accountService.save(user2)

        User user3 = new User('more awesome')
        accountService.save(user3)

        and:
        HttpRequest getReq = HttpRequest.GET("/users?offset=${user.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<UserDto> users= body.get("users") as List<UserDto>
        users.first().with {
            assert id == user2.id
            assert name == user2.name
            assert dateCreated
        }
    }




    def "Should throw not found exception on delete no found user"(){
        given:
        HttpRequest request = HttpRequest.DELETE("/users/666")

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<User>, Argument.of(UserNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<UserNotFoundException> jsonError = e.response.getBody(UserNotFoundException)

        then:
        jsonError.isPresent()
        jsonError.get().message == USER_NOT_FOUND_MESSAGE
    }

    def "Should delete an user"() {
        given:'a saved user'
        User user = new User('i will die soon cause i have covid-19')
        accountService.save(user)
        Long id = user.id

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("/users/${id}")

        when:
        def response = client.toBlocking().exchange(request, UserDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest getReq = HttpRequest.GET("/users/${id}")

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<User>, Argument.of(UserNotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<UserNotFoundException> jsonError = e.response.getBody(UserNotFoundException)

        then:
        jsonError.isPresent()
        jsonError.get().message == USER_NOT_FOUND_MESSAGE

    }


}
