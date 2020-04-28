package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.pogos.UserCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
class User  implements GormEntity<User> {

    String name
    Date dateCreated

    User(UserCreateCommand cmd) {
        this.name = cmd.name
    }

    static constraints = {
        name nullable: false, blank:false
    }

    static mapping = {
        autoTimestamp true
    }
}
