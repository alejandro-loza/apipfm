package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.dtos.resource.WebhookDto
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.enums.BudgetStatusEnum
import mx.finerio.pfm.api.enums.WebhookNatureEnum
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.WebhookGormService
import mx.finerio.pfm.api.validation.UserCommand
import mx.finerio.pfm.api.validation.WebHookCreateCommand
import mx.finerio.pfm.api.validation.WebHookUpdateCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject


@Property(name = 'spec.name', value = 'web hook controller')
@MicronautTest(application = Application.class)

class WebHookControllerSpec extends Specification {

    public static final String LOGIN_ROOT = "/login"
    public static final String WEBHOOK_ROOT = "/webhooks"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Shared
    mx.finerio.pfm.api.domain.Client loggedInClient

    @Shared
    String accessToken

    @Inject
    @Shared
    ClientService clientService

    @Inject
    WebhookGormService webhookGormService

    def setupSpec(){
        def generatedUserName = this.getClass().getCanonicalName()
        loggedInClient = clientService.register( generatedUserName, 'elementary', ['ROLE_ADMIN'])
        HttpRequest request = HttpRequest.POST(LOGIN_ROOT, [username:generatedUserName, password:'elementary'])
        def rsp = client.toBlocking().exchange(request, AccessRefreshToken)
        accessToken = rsp.body.get().accessToken
    }

    void cleanup(){
        webhookGormService.findAll().each {
            webhookGormService.delete(it.id)
        }
    }

    def "Should create and get a webhook"(){
        given:'an webhook'
        WebHookCreateCommand cmd = new WebHookCreateCommand()
        cmd.with {
            url = "www.test.com"
            nature = WebhookNatureEnum.budget_status
        }

        HttpRequest request = HttpRequest.POST(WEBHOOK_ROOT, cmd).bearerAuth(accessToken)

        when:
        def rsp = client.toBlocking().exchange(request, WebhookDto)

        then:
        rsp.status == HttpStatus.OK
        assert  rsp.body().url == cmd.url
        assert rsp.body().nature == cmd.nature.toString()
        assert rsp.body().id
    }

    def "Should get an webhook"(){
        given:'a saved webhook'
        Webhook webhook = generateWebhook()

        and:
        HttpRequest getReq = HttpRequest.GET("${WEBHOOK_ROOT}/${webhook.id}").bearerAuth(accessToken)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.of(WebhookDto) as Argument<WebhookDto>,
                Argument.of(ErrorDto))

        then:
        assert rspGET.status == HttpStatus.OK
        assert rspGET.body().url == webhook.url
        assert rspGET.body().nature == webhook.nature.toString()
        assert rspGET.body().id == webhook.id

    }

    def "Should not get an deleted webhook"(){
        given:'a saved but deleted webhook'
        Webhook webhook = new Webhook()
        webhook.with {
            url = "www.google.com"
            nature = BudgetStatusEnum.ok
            webhook.client = loggedInClient
            dateDeleted = new Date()
        }
        webhookGormService.save(webhook)


        and:'a client'
        HttpRequest request = HttpRequest.GET("${WEBHOOK_ROOT}/${webhook.id}").bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(WebhookDto) as Argument<WebhookDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
        e.response.status.code == 404

    }


    def "Should update an webhook"(){
        given:'a saved webhook'
        Webhook webhook =  generateWebhook()

        and:'an user command to update data'
        WebHookUpdateCommand cmd = new WebHookUpdateCommand()
        cmd.with {
            url = "www.fakeurl.com"
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${WEBHOOK_ROOT}/${webhook.id}",  cmd).bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, WebhookDto)

        then:
        assert response.status == HttpStatus.OK
        assert response.body().url == cmd.url
        assert response.body().nature == webhook.nature.toString()
        assert response.body().id == webhook.id

    }

    def "Should not update an deleted webhook"(){
        given:'a saved but deleted webhook'
        Webhook webhook = new Webhook()
        webhook.with {
            url = "www.google.com"
            nature = BudgetStatusEnum.ok
            webhook.client = loggedInClient
            dateDeleted = new Date()
        }
        webhookGormService.save(webhook)

        and:'an user command to update data'
        WebHookUpdateCommand cmd = new WebHookUpdateCommand()
        cmd.with {
            url = "www.fakeurl.com"
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${WEBHOOK_ROOT}/${webhook.id}",  cmd).bearerAuth(accessToken)

        when:
        client.toBlocking().exchange(request, Argument.of(WebhookDto) as Argument<WebhookDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
        e.response.status.code == 404

    }


    def "Should delete a webhook"() {
        given:'a saved webhook'
        Webhook webhook =  generateWebhook()

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${WEBHOOK_ROOT}/${webhook.id}").bearerAuth(accessToken)

        when:
        def response = client.toBlocking().exchange(request, WebhookDto)

        then:
        response.status == HttpStatus.NO_CONTENT
        assert webhookGormService.findById(webhook.id).dateDeleted


    }


    private Webhook generateWebhook() {
        Webhook webhook = new Webhook()
        webhook.with {
            url = "www.google.com"
            nature = BudgetStatusEnum.ok
            webhook.client = loggedInClient
        }
        webhookGormService.save(webhook)
        webhook
    }


}
