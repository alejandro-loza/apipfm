package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.validation.SignupCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'signup controller')
@MicronautTest(application = Application.class)
class SignupControllerSpec  extends Specification {
    public static final String LOGIN_ROOT = "/login"
    public static final String SIGNUP_ROOT = "/signup"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    @Shared
    ClientService clientService


    @Shared
    String accessToken

    @Shared
    mx.finerio.pfm.api.domain.Client loggedInClient

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient = clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
    }

    def "Should do a signup"(){
        given:
        SignupCommand cmd = new SignupCommand()
        cmd.with {


        }
    }

}
