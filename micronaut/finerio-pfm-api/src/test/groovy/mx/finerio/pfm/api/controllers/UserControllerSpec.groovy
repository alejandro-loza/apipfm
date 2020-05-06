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
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exeptions.UserNotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.validation.UserCreateCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'usercontroller')
@MicronautTest(application = Application.class)
class UserControllerSpec extends Specification {

    public static final String USER_NOT_FOUND_MESSAGE = 'The user ID you requested was not found.'
    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    UserService userService

    def "Should get a empty list of users"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET("/users")

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(UserDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()

    }

    def "Should create and get user"(){
        given:'an user'
        UserCreateCommand cmd = new UserCreateCommand()
        cmd.with {
            name = 'username'
        }

        HttpRequest request = HttpRequest.POST('/users', cmd)

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
        userService.save(user)

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
        userService.save(user)

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
         userService.save(user)

         User user2 = new User('awesome')
         userService.save(user2)
        and:
        HttpRequest getReq = HttpRequest.GET("/users")

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(UserDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().size() > 0
        rspGET.body().first().class.name == "mx.finerio.pfm.api.dtos.UserDto"

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
        userService.save(user)
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
