package mx.finerio.pfm.api.services.gorm

import mx.finerio.pfm.api.domain.User
import grails.gorm.services.Service

@Service(User)
interface UserServiceRepository {
    User save(User user)
    User getById(Long id)
    List<User> findAll(Map args)
    void delete(Long id)
}