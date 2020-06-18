package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Transaction

@Service(Category)
interface CategoryGormService {
    Category save(Category category)
    Category getById(Long id)
    List<Category> findAllByDateDeletedIsNull(Map args)
    List<Category> findAllByDateDeletedIsNullAndIdLessThanEquals(Long id, Map args)
    List<Category> findAll()
    void delete(Serializable id)
    @Query("from ${Category c} where $c.id = $id and c.dateDeleted is Null")
    Category findByIdAndDateDeletedIsNull(Long id)
}