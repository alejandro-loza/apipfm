package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.clients.CallbackRestService
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.enums.BudgetStatusEnum
import mx.finerio.pfm.api.services.gorm.WebhookGormService
import mx.finerio.pfm.api.services.imp.WebhookServiceImp
import spock.lang.Specification

@Property(name = 'spec.name', value = 'WebhooksService service')
@MicronautTest(application = Application.class)
class WebhooksServiceSpec extends Specification {

    WebhookService webhookService = new WebhookServiceImp()

    void setup(){
        webhookService.webhookGormService = Mock(WebhookGormService)
        webhookService.callbackRestService = Mock(CallbackRestService)
        webhookService.budgetService = Mock(BudgetService)
    }

    def "Should alert on budget amount exceeded"(){
        given:

        def user = new User()
        user.client = new Client()

        def account = new Account()
        account.with {
            account.user = user
        }
        Transaction transaction = new Transaction()
        transaction.with {
            transaction.account = account
        }

        def budgetDto = new BudgetDto()
        budgetDto.with {
            status = BudgetStatusEnum.warning
        }

        def webhook = new Webhook()
        webhook.url = "www.fake.com"

        when:
        webhookService.verifyAndAlertTransactionBudgetAmount(transaction)

        then:

        1 * webhookService.budgetService.findTransactionBudget(_ as Transaction) >> budgetDto
        1 * webhookService.webhookGormService.findByClientAndDateDeletedIsNull(_ as Client, _ as String ) >> webhook
        1 * webhookService.callbackRestService.post(_ as String, _ as Map)

    }

}
