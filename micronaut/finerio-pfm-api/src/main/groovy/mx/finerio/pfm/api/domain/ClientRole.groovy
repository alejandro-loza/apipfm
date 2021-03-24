package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false)
class ClientRole implements GormEntity<ClientRole> {

    Client client
    Role role

    static constraints = {
        id generator: 'native', params:[sequence:'CLIENT_ROLE_SEQ']
        client nullable: false
        role nullable: false
    }

}
