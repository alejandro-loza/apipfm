package mx.finerio.pfm.api.dtos.resource

import mx.finerio.pfm.api.domain.Webhook

class WebhookDto extends ResourceDto {
    String url
    String nature

    WebhookDto() {}

    WebhookDto(Webhook webhook) {
        this.id = webhook.id
        this.url = webhook.url
        this.nature = webhook.nature
    }
}
