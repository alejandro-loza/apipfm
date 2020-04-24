package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.User
import grails.gorm.services.Service
import groovy.transform.CompileStatic

@Service(User)
@CompileStatic
interface UserService {
    User save(User user)
}