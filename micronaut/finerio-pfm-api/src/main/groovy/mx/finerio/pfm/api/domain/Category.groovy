package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity

import groovy.transform.ToString

import mx.finerio.pfm.api.validation.CategoryCreateCommand

import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false)
class Category implements GormEntity<Category> {

    Long id
    User user
    String name
    String color
    Category parent
    Date dateCreated
    Date lastUpdated
    Date dateDeleted
    Client client

    Category(CategoryCreateCommand cmd, User user, Client client) {
        this.user = user
        this.name = cmd.name
        this.color = cmd.color
        this.client = client
    }

    Category(){}

    static constraints = {
        user nullable: true
        name nullable: false, blank:false
        color  nullable: false, blank:false
        dateDeleted nullable:true
        parent nullable:true
    }

    static mapping = {
        autoTimestamp true
    }

    boolean isSubcategory(){
        this?.parent
    }

}
