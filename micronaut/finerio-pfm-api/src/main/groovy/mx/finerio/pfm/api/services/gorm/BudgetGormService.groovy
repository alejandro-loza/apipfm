package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Transaction

@Service(Transaction)
interface BudgetGormService {
    Transaction save(Transaction transaction)
    Transaction getById(Long id)
    List<Transaction> findAllByDateDeletedIsNull(Map args)
    List<Transaction> findAllByDateDeletedIsNullAndIdLessThanEquals(Long id, Map args)

    @Query("from ${Transaction a} where $a.id = $id and a.dateDeleted is Null")
    Transaction findByIdAndDateDeletedIsNull(Long id)
}