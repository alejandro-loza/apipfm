package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.clients.CallbackRestService
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.dtos.resource.WebhookDto
import mx.finerio.pfm.api.enums.BudgetStatusEnum
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.BudgetService
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

    @Inject
    CallbackRestService callbackRestService

    @Inject
    BudgetService budgetService

    @Override
    Webhook find(long id) {
        Optional.ofNullable(webhookGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('webhook.notFound') })
    }

    @Override
    Webhook create(WebHookCreateCommand cmd) {
        verifyBody(cmd)
        return webhookGormService.save(new Webhook(cmd, getCurrentLoggedClient()))
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

    @Override
    void alertUserClientWebHook(Client client, BudgetStatusEnum nature ) {
        Webhook webhook =  webhookGormService.findByClientAndNatureAndDateDeletedIsNull(client, nature.toString())
        if (webhook){
            callbackRestService.post(webhook.url,["test":"test"])//todo change with correct payload
        }
    }

    @Override
    void verifyAndAlertTransactionBudgetAmount(Transaction transaction){
        BudgetDto budgetDto = budgetService.findTransactionBudget(transaction)
        if(budgetDto && budgetDto.status != BudgetStatusEnum.ok){
            alertUserClientWebHook(transaction.account.user.client, budgetDto.status)
        }
    }

}
