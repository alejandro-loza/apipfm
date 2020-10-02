package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.SystemCategory

@Service(SystemCategory)
interface SystemCategoryGormService {
    List<SystemCategory> findAll()
    @Query("from ${SystemCategory sc} where $sc.finerioConnectId = $finerioConnectId")
    SystemCategory findByFinerioConnectId(String finerioConnectId)
}