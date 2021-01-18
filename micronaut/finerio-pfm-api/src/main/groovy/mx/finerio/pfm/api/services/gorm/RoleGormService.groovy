package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Role

@Service(Role)
interface RoleGormService {

    Role save(String authority )
    Role find( String authority )

}