package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import grails.gorm.services.Service

@Service(User)
interface UserGormService {

    User save(User user)

    @Query("from ${User u} where $u.id = $id and u.dateDeleted is Null")
    User findByIdAndDateDeletedIsNull(Long id)

    @Query("from ${User u} where $u.id = $id and u.client = $client and u.dateDeleted is Null")
    User findByIdAndClientAndDateDeletedIsNull(Long id,Client client)

    @Query("from ${User u} where $u.name = $name and u.client = $client and u.dateDeleted is Null")
    User findByNameAndAndClientAndDateDeletedIsNull(String name, Client client)
    User findById(Long id)
    List<User> findAllByDateDeletedIsNull(Map args)
    List<User> findAllByClientAndDateDeletedIsNullAndIdLessThanEquals(Client client, Long cursor, Map args)
    List<User> findAllByClientAndDateDeletedIsNull(Client client, Map args)
    List<User> findAllByDateDeletedIsNullAndIdLessThanEquals(Long id, Map args)
    List<User> findAll()
    void delete(Serializable id)
}