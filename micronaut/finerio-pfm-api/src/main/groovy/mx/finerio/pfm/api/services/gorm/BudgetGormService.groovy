package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Budget

@Service(Budget)
interface BudgetGormService {
    Budget save(Budget transaction)
    Budget getById(Long id)
    List<Budget> findAllByDateDeletedIsNull(Map args)
    List<Budget> findAllByDateDeletedIsNullAndIdLessThanEquals(Long id, Map args)
    List<Budget> findAll()
    @Query("from ${Budget a} where $a.id = $id and a.dateDeleted is Null")
    Budget findByIdAndDateDeletedIsNull(Long id)
    void delete(Serializable id)
}