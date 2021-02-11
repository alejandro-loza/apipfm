package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.dtos.resource.WebhookDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.WebhookService
import mx.finerio.pfm.api.services.gorm.WebhookGormService
import mx.finerio.pfm.api.validation.WebHookCreateCommand
import mx.finerio.pfm.api.validation.WebHookUpdateCommand
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service
class WebhookServiceImp extends ServiceTemplate implements WebhookService {

    @Inject
    WebhookGormService webhookGormService

    @Override
    Webhook find(long id) {
        Optional.ofNullable(webhookGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('webhook.notFound') })
    }

    @Override
    Webhook create(WebHookCreateCommand cmd) {
        verifyBody(cmd)
        return webhookGormService.save(new Webhook(cmd))
    }

    @Override
    @Transactional
    Webhook update(WebHookUpdateCommand cmd, Long id) {
        verifyBody(cmd)
        Webhook webhook = find(id)
        webhook.with {
            url = cmd.url ?: url
            nature = cmd.nature ?: nature
        }
        webhookGormService.save(webhook)
    }

    @Override
    @Transactional
    List<WebhookDto> getAll() {
        webhookGormService.findAll().collect{new WebhookDto(it)}
    }

    @Override
    @Transactional
    void delete(Webhook webhook){
        webhook.dateDeleted = new Date()
        webhookGormService.save(webhook)
    }

}
