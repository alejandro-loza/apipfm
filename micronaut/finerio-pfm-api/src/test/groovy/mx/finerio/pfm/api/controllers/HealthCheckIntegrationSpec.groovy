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
import mx.finerio.pfm.api.services.ClientService
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'health endpoint')
@MicronautTest(application = Application.class)
class HealthCheckIntegrationSpec extends Specification {
    public static final String HEALTH_ROOT = "/health"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client


    def "Should do a health check"(){
        given:

        HttpRequest getReq = HttpRequest.GET(HEALTH_ROOT)


        when:
        def rsp = client.toBlocking().exchange(getReq, Map)


        then:
        noExceptionThrown()
        rsp.status == HttpStatus.OK
        assert rsp.body.get()["status"] == 'UP'

         }

}
