package mx.finerio.pfm.api.services

import grails.gorm.services.Query
import mx.finerio.pfm.api.domain.User
import grails.gorm.services.Service

@Service(User)
interface UserService {
    User save(User user)

    @Query("from ${User u} where $u.id = $id and u.dateDeleted is Null")
    User findByIdAndDateDeletedIsNull(Long id)
    User findById(Long id)
    List<User> findAll(Map args)
    List<User> findByIdLessThanEquals(Long id, Map args)
}