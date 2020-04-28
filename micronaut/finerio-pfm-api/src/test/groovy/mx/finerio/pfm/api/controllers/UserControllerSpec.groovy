package mx.finerio.pfm.api.controllers


import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.pogos.UserCreateCommand
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

    def "Should create an user"(){
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

    }

    def "Should not create an user an return 400"(){
        given:'an user'

        HttpRequest request = HttpRequest.POST('/users',  new UserCreateCommand())

        when:
        client.toBlocking().exchange(request)

        then:
        thrown HttpClientResponseException
    }
}
