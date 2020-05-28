package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

@Entity
class ClientRole implements GormEntity<ClientRole> {

    Client client
    Role role

    static constraints = {
        client nullable: false
        role nullable: false
    }

}