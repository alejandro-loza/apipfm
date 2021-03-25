package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.Webhook
import mx.finerio.pfm.api.enums.BudgetStatusEnum

@Service(Webhook)
interface WebhookGormService {

    Webhook save(Webhook webhook)

    @Query("from ${Webhook w} where $w.id = $id and $w.dateDeleted is Null")
    Webhook findByIdAndDateDeletedIsNull(Long id)

    Webhook findById(Long id)

    List<Webhook> findAll()

    Webhook findByClientAndDateDeletedIsNull(Client client)

    void delete(Serializable id)
}