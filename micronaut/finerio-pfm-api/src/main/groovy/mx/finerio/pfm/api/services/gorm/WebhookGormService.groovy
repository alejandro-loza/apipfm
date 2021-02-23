package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.enums.BudgetStatusEnum

@Service(Webhook)
interface WebhookGormService {

    Webhook save(Webhook webhook)

    Webhook findByIdAndDateDeletedIsNull(Long id)

    Webhook findById(Long id)

    List<Webhook> findAll()

    Webhook findByClientAndNatureAndDateDeletedIsNull(Client client, BudgetStatusEnum nature)

    void delete(Serializable id)
}