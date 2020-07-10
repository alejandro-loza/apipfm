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
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.UserCommand
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
        List<User> users = userGormService.findAll()
        users.each {  user ->
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

    def "Should get an user"(){
        given:'a saved user'
        User user =  generateUser()

        and:
        HttpRequest getReq = HttpRequest.GET("${USER_ROOT}/${user.id}").bearerAuth(accessToken)

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
        HttpRequest request = HttpRequest.POST(USER_ROOT,  new UserCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,
                Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an user with wrong body an return 400"(){
        given:'an user'

        HttpRequest request = HttpRequest.POST(USER_ROOT,  'qwe').bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,
                Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorDto> jsonError = e.response.getBody(ErrorDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().with {
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
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,  Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        when:
        Optional<ErrorDto> jsonError = e.response.getBody(ErrorDto)
        then:
        assert jsonError.isPresent()
        jsonError.get().with {
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
        assert body.get("nextCursor") == user2.id -1

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

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${USER_ROOT}/${id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, UserDto)

        then:
        response.status == HttpStatus.NO_CONTENT
        assert userGormService.findById(user.id).dateDeleted


    }

    private User generateUser() {
        userGormService.save(new User('awesome user', loggedInClient))
    }

    private mx.finerio.pfm.api.domain.Client generateClient(){
        clientService.register("another client", 'elementary', ['ROLE_DETECTIVE'])
    }


}
