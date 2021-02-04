package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false)
class ClientProfile implements GormEntity<ClientProfile> {

    Long id
    Client client
    String name
    String firstLastName
    String secondLastName
    String email
    String companyName
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    static constraints = {
        name nullable: false, blank: false, size: 1..50
        firstLastName nullable: false, blank: false, size: 1..20
        secondLastName nullable: false, blank: false, size: 1..20
        email nullable: false, blank: false, size: 1..100
        companyName nullable: false, blank: false, size: 1..20
        dateDeleted nullable: true
    }

}
