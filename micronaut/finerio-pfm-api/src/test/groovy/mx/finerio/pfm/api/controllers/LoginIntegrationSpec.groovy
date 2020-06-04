package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
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

    @Shared
    @Inject
    JwtTokenValidator tokenValidator


    def "Should do login"(){
        given:
        registerService.register( "sherlock", 'elementary', ['ROLE_DETECTIVE'])

        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:'sherlock', password:'elementary'])

        when:
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        noExceptionThrown()
        rsp.status == HttpStatus.OK
        rsp.body.get().accessToken
        rsp.body.get().refreshToken
        rsp.body().expiresIn

        when:
        String accessToken = rsp.body.get().accessToken
        Authentication authentication = Flowable.fromPublisher(tokenValidator.validateToken(accessToken)).blockingFirst()

        then:
        authentication.getAttributes()
        authentication.getAttributes().containsKey('roles')
        authentication.getAttributes().containsKey('iss')
        authentication.getAttributes().containsKey('exp')
        authentication.getAttributes().containsKey('iat')

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
