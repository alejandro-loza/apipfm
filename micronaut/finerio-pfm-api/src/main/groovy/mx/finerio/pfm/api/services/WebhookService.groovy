package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.dtos.resource.WebhookDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.WebHookCreateCommand
import mx.finerio.pfm.api.validation.WebHookUpdateCommand

interface WebhookService {
    @Log
    Webhook find(long id)

    @Log
    Webhook create(WebHookCreateCommand cmd)

    @Log
    Webhook update(WebHookUpdateCommand cmd, Long id)

    @Log
    List<WebhookDto> getAll()

    @Log
    void delete(Webhook webhook)
}