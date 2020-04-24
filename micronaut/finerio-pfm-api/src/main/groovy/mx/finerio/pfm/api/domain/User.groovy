package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.pogos.UserCreateCommand

@Entity
class User {
    String username
    String firstName
    String lastName
    String email

    User(UserCreateCommand cmd) {
        this.username = cmd.username
        this.firstName = cmd.firstName
        this.lastName = cmd.lastName
        this.email = cmd.email
        this.phone = cmd.phone
        this.userStatus = cmd.userStatus
    }
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
