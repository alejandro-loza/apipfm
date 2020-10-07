package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
class Category implements GormEntity<Category> {

    Long id
    String name
    String color
    Date dateCreated
    Date lastUpdated
    User user
    Client client
    Date dateDeleted
    Category parent

    Category(CategoryCreateCommand cmd,  Client client) {
        this.name = cmd.name
        this.color = cmd.color
        this.client = client
    }

    Category(){}

    static constraints = {
        user nullable: true
        dateDeleted  nullable: true
        parent  nullable: true
        color  nullable: true
    }

}
