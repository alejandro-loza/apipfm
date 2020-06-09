package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.security.token.jwt.validator.JwtTokenValidator
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.Flowable
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.RegisterService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.UserCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'usercontroller')
@MicronautTest(application = Application.class)
class UserControllerSpec extends Specification {

    public static final String LOGIN_ROOT = "/login"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    UserGormService userService

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

    def "Should get a empty list of users"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET("/users").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(UserDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()

    }

    def "Should create and get user"(){
        given:'an user'
        UserCommand cmd = new UserCommand()
        cmd.with {
            name = 'username'
        }

        HttpRequest request = HttpRequest.POST('/users', cmd).bearerAuth(accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, UserDto)

        then:
        rsp.status == HttpStatus.OK
        rsp.body().name == cmd.name
    }

    def "Should get an user"(){
        given:'a saved user'
        User user = new User('no awesome name')
        userService.save(user)

        and:
        HttpRequest getReq = HttpRequest.GET("/users/${user.id}").bearerAuth(accessToken)

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
        HttpRequest request = HttpRequest.POST('/users',  new UserCommand()).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,
                Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an user with wrong body an return 400"(){
        given:'an user'

        HttpRequest request = HttpRequest.POST('/users',  'qwe').bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<UserDto>,
                Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should throw not found exception on no found user"(){
        given:'a not found if'

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.GET("/users/${notFoundId}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(User) as Argument<User>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should update an user"(){
        given:'a saved user'
        User user = new User('no awesome name')
        userService.save(user)

        and:'an user command to update data'
        UserCommand cmd = new UserCommand()
        cmd.with {
            name = 'awesome name'
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("/users/${user.id}",  cmd).bearerAuth(accessToken)

        when:
        def resp = client.toBlocking().exchange(request, UserDto)

        then:
        resp.status == HttpStatus.OK
        resp.body().name == cmd.name

    }

    def "Should not update an user on band parameters and return Bad Request"(){
        given:'a saved user'
        User user = new User('no awesome name')
        userService.save(user)

        and:'a client'
        HttpRequest request = HttpRequest.PUT("/users/${user.id}",  new UserCommand()).bearerAuth(accessToken)

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
        HttpRequest request = HttpRequest.PUT("/users/${notFoundId}",  cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(User) as Argument<User>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of users"(){

         given:'a saved user'
         User user = new User('no awesome')
         user.dateDeleted = new Date()
         userService.save(user)

         User user2 = new User('awesome')
         userService.save(user2)
        and:
        HttpRequest getReq = HttpRequest.GET("/users").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<UserDto> users= body.get("data") as List<UserDto>
        assert users.size() > 0
        assert !(user.id in users.id)

    }

    def "Should get a list of users in a cursor point"() {

        given:'a saved user'
        User user = new User('no awesome')
        userService.save(user)

        User user2 = new User('awesome')
        userService.save(user2)

        User user3 = new User('more awesome')
        userService.save(user3)

        and:
        HttpRequest getReq = HttpRequest.GET("/users?cursor=${user2.id}").bearerAuth(accessToken)

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
    }

    def "Should throw not found exception on delete no found user"(){
        given:
        HttpRequest request = HttpRequest.DELETE("/users/666").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<User>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete an user"() {
        given:'a saved user'
        User user = new User('i will die soon cause i have covid-19')
        userService.save(user)
        Long id = user.id

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("/users/${id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, UserDto)

        then:
        response.status == HttpStatus.NO_CONTENT
        assert userService.findById(user.id).dateDeleted

        and:
        HttpRequest.GET("/users/${id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(UserDto) as Argument<User>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }


}
