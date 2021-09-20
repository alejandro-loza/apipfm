package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.RequestLogger

@Service(RequestLogger)
interface RequestLoggerGormService {
    RequestLogger save(RequestLogger requestLogger)
    List<RequestLogger> findAll()
    void delete(Serializable id)

}