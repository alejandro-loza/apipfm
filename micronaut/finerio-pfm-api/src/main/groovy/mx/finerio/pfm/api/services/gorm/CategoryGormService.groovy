package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User

@Service(Category)
interface CategoryGormService {
    Category save(Category category)
    Category getById(Long id)
    List<Category> findAllByDateDeletedIsNull(Map args)
    List<Category> findAllByDateDeletedIsNullAndIdLessThanEquals(Long id, Map args)
    List<Category> findAll()
    List<Category> findAllByParentIsNotNull()
    List<Category> findAllByClientAndUserIsNullAndDateDeletedIsNull(Client client, Map args)
    List<Category> findAllByUserAndDateDeletedIsNull(User user, Map args)
    void delete(Serializable id)
    @Query("from ${Category c} where $c.id = $id and c.dateDeleted is Null")
    Category findByIdAndDateDeletedIsNull(Long id)

    @Query("update ${Category c} set c.dateDeleted = ${new Date()} where $c.parent = $category")
    void deleteAllByParentCategory(Category category)
}