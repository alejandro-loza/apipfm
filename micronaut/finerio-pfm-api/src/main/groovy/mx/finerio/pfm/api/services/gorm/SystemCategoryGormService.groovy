package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.SystemCategory

@Service(SystemCategory)
interface SystemCategoryGormService {
    List<SystemCategory> findAll()
    SystemCategory findByFinerioConnectId(String id)
}