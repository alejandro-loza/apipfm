package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.dtos.resource.WebhookDto
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.*
import mx.finerio.pfm.api.validation.UserCommand
import mx.finerio.pfm.api.validation.WebHookCreateCommand
import mx.finerio.pfm.api.validation.WebHookUpdateCommand

import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static io.reactivex.Single.just

@Controller("/webhooks")
@Validated
@Secured('isAuthenticated()')
class WebhookController {

    @Inject
    WebhookService webhookService

    @Inject
    NextCursorService nextCursorService

    @Log
    @Post("/")
    Single<WebhookDto> save(@Body @Valid WebHookCreateCommand cmd){
        just(new WebhookDto(webhookService.create(cmd)))
    }

    @Log
    @Get("/{id}")
    Single<WebhookDto> show(@NotNull Long id) {
        just(new WebhookDto(webhookService.find(id)))
    }

    @Log
    @Get
    Single<ResourcesDto>  showAll() {
        nextCursorService.generateResourcesDto(webhookService.getAll())
    }

    @Log
    @Put("/{id}")
    Single<WebhookDto> edit(@Body @Valid WebHookUpdateCommand cmd, @NotNull Long id ) {
        just(new WebhookDto(webhookService.update(cmd,id)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        Webhook webhook = webhookService.find(id)
        webhookService.delete(webhook)
        HttpResponse.noContent()
    }

}
