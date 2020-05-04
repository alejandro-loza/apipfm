package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

@Entity
class User  implements GormEntity<User> {

    Long id
    String name
    Date dateCreated

    User(String name) {
        this.name = name
    }

    User() {}

    static constraints = {
        name nullable: false, blank:false
    }

    static mapping = {
        autoTimestamp true
    }
}
