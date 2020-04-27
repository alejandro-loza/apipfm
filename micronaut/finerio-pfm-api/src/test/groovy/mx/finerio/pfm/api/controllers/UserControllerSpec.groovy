package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Rollback
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.pogos.UserCreateCommand
import mx.finerio.pfm.api.services.UserService
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'userontroller')
@MicronautTest(application = Application.class)
class UserControllerSpec extends Specification {

    @Shared
    @Inject
    ApplicationContext applicationContext

    @Shared
    @Inject
    @Client("/")
    RxHttpClient client



    def "Should create an user"(){
        given:'an user'
        UserCreateCommand cmd = new UserCreateCommand()
        cmd.with {
            username = 'username'
            firstName = 'firstName'
            lastName = 'lastName'
            email = 'test@test.com'
            phone = 666
            userStatus = 1
        }

        HttpRequest request = HttpRequest.POST('/users', cmd)


        when:
        HttpResponse rsp = client.toBlocking().exchange(request)


        then:
        rsp.status == HttpStatus.OK


    }
}
