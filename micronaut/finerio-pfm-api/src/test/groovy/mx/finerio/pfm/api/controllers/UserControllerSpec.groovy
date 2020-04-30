package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.UserCreateCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'usercontroller')
@MicronautTest(application = Application.class)
class UserControllerSpec extends Specification {

    @Shared
    @Inject
    @Client("/")
    RxHttpClient client

    def "Should create and get user"(){
        given:'an user'
        UserCreateCommand cmd = new UserCreateCommand()
        cmd.with {
            name = 'username'
        }

        HttpRequest request = HttpRequest.POST('/users', cmd)

        when:
        def rsp = client.toBlocking().exchange(request)

        then:
        rsp.status == HttpStatus.OK

        HttpRequest getReq = HttpRequest.GET('/users/1')

        when:
        def rspGET = client.toBlocking().exchange(getReq)

        then:
        rspGET.status == HttpStatus.OK
    }

    def "Should not create an user an return 400"(){
        given:'an user'

        HttpRequest request = HttpRequest.POST('/users',  new UserCreateCommand())

        when:
        client.toBlocking().exchange(request)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

}
