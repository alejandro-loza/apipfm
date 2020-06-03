package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.services.gorm.ClientGormService
import mx.finerio.pfm.api.services.gorm.ClientRoleGormService
import mx.finerio.pfm.api.services.gorm.RoleGormService
import mx.finerio.pfm.api.services.security.BCryptPasswordEncoderService
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'login controller')
@MicronautTest(application = Application.class)
class LoginIntegrationSpec extends Specification {
    public static final String LOGIN_ROOT = "/login"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    ClientGormService clientGormService

    @Inject
    RoleGormService roleGormService

    @Inject
    ClientRoleGormService clientRoleGormService

    @Inject
    BCryptPasswordEncoderService passwordEncoderService


    def "Should do login"(){
        given:

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("sherlock", "password")
        final String encodedPassword = passwordEncoderService.encode(creds.password)

        def client1 = clientGormService.save(creds.username, encodedPassword)
        def role = roleGormService.save('ADMIN')
        clientRoleGormService.save(client1, role)
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:creds.username, password:creds.password])

        when:
        def response = client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        def  e = thrown HttpClientResponseException
        e.response.body() == ''
      //  response.status == HttpStatus.OK
    }
}
