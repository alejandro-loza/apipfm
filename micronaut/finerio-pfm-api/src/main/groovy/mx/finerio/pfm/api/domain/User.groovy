package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

@Entity
class User  implements GormEntity<User> {

    Long id
    String name
    Date dateCreated
    Date dateDeleted

    User(String name) {
        this.name = name
    }

    User() {}

    static constraints = {
        name nullable: false, blank:false
        dateDeleted nullable:true
    }

    static mapping = {
        autoTimestamp true
    }
}
