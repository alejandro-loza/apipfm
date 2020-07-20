package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity

import groovy.transform.ToString

import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false)
class User  implements GormEntity<User> {

    Long id
    String name
    Client client
    Date dateCreated
    Date dateDeleted

    User(String name, Client client) {
        this.name = name
        this.client = client
    }

    User() {}

    static constraints = {
        name nullable: false, blank:false
        dateDeleted nullable:true
        client nullable: false
    }

    static mapping = {
        autoTimestamp true
    }
}
