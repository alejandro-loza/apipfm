package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import javax.persistence.Table

@Entity
class User {
    String username
    String firstName
    String lastName
    String email
    Long phone
    int userStatus

    static constraints = {
        username nullable: false
        firstName nullable: false
        lastName  nullable: false
        email nullable: false, email: true
        phone nullable: false
        userStatus nullable: false
    }

    static mapping = {
        autoTimestamp true
    }
}
