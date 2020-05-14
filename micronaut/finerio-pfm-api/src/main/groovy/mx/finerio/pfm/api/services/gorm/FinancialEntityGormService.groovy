package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.FinancialEntity

@Service(FinancialEntity)
interface FinancialEntityGormService {
    FinancialEntity save(FinancialEntity account)
    @Query("from ${FinancialEntity u} where $u.id = $id and u.dateDeleted is Null")
    FinancialEntity findByIdAndDateDeletedIsNull(Long id)
    FinancialEntity getById(Long id)
    List<FinancialEntity> findAll(Map args)//todo should not include deleted ones
    List<FinancialEntity> findAllByDateDeletedIsNull(Map args)
    List<FinancialEntity> findAllByDateDeletedIsNullAndIdLessThanEquals(Long id, Map args)
}