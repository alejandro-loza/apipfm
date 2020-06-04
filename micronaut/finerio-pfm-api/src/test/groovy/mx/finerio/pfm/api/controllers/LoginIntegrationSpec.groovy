package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.services.RegisterService
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

    def "Should not do login"(){
        given:
        registerService.register( "admin", 'elementary', ['ROLE_DETECTIVE'])

        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:'admin', password:'wrongpass'])

        when:
        client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.UNAUTHORIZED
    }

    def "Should not do login on not found client"(){
        given:
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:'hacker', password:'hacker666'])

        when:
        client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.UNAUTHORIZED
    }
}
