package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Client

@Service(Client)
interface ClientGormService {

    Client save(String username, String password )

    Client findByUsername( String username )

    Client findById(Serializable id)

}