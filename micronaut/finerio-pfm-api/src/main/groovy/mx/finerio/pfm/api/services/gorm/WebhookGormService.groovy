package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Webhook

@Service(Webhook)
interface WebhookGormService {

    Webhook save(Webhook webhook)

    Webhook findByIdAndDateDeletedIsNull(Long id)

    Webhook findById(Long id)

    List<Webhook> findAll()
    void delete(Serializable id)
}