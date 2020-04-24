package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Rollback
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.pogos.UserCreateCommand
import mx.finerio.pfm.api.services.UserService
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Rollback
@MicronautTest
class UserControllerSpec extends Specification {


    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())

    def cleanup() {
        deleteAllTodo()
    }


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

        when:
        client.toBlocking().exchange(HttpRequest.POST("/users", cmd))

        then:
        client.toBlocking().retrieve("/users", List)*.username == ["TEST_TITLE"]

    }
}
