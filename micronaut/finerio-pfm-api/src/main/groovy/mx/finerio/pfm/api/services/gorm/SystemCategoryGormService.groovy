package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.SystemCategory

@Service(SystemCategory)
interface SystemCategoryGormService {
    List<SystemCategory> findAll()
    @Query("from ${SystemCategory sc} where $sc.finerioConnectId = $finerioConnectId")
    SystemCategory findByFinerioConnectId(String finerioConnectId)

    @Query("from ${SystemCategory c} where $c.id = $id and c.dateDeleted is Null")
    SystemCategory findByIdAndDateDeletedIsNull(Long id)
}