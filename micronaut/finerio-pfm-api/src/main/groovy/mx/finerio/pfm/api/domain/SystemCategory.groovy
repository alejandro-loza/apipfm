package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

@Entity
class SystemCategory implements GormEntity<SystemCategory> {

    Long id
    String name
    String color
    Date dateCreated
    Date lastUpdated
    Date dateDeleted
    SystemCategory parent
    String finerioConnectId

    static constraints = {
        parent nullable:true
        color nullable:true
        dateDeleted nullable:true
    }

    static mapping = {
        autoTimestamp true
    }
}
