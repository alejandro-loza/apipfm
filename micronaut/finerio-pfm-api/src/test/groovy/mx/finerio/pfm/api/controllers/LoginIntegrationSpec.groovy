package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.services.RegisterService
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
    RegisterService registerService

    def "Should do login"(){
        given:
        registerService.register( "sherlock", 'elementary', ['ROLE_DETECTIVE'])

        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:'sherlock', password:'elementary'])

        when:
        def response = client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        response.status == HttpStatus.OK
        response.body.get().accessToken
        response.body.get().refreshToken
        response.body().expiresIn

    }
}
