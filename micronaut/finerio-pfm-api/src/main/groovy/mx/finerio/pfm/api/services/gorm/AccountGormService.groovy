package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.User

@Service(Account)
interface AccountGormService {
    Account save(Account account)
    Account getById(Long id)
    List<Account> findAllByDateDeletedIsNull(Map args)
    List<Account> findAllByDateDeletedIsNullAndIdLessThanEquals(Long id, Map args)
    List<Account> findAllByUserAndDateDeletedIsNullAndIdLessThanEquals(User user,Long cursor, Map args)
    List<Account> findAllByUserAndDateDeletedIsNull(User user, Map args)
    List<Account> findAll()
    void delete(Serializable id)
    @Query("from ${Account a} where $a.id = $id and a.dateDeleted is Null")
    Account findByIdAndDateDeletedIsNull(Long id)
}