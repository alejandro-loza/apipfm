package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.validation.CategoryCommand
import org.grails.datastore.gorm.GormEntity

@Entity
class Category implements GormEntity<Category> {

    Long id
    User user
    String name
    String color
    Category parent
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    Category(CategoryCommand cmd, User user) {
        this.user = user
        this.name = cmd.name
        this.color = cmd.color
    }

    Category(){}

    static constraints = {
        user nullable: false
        name nullable: false, blank:false
        color  nullable: false, blank:false
        dateDeleted nullable:true
        parent nullable:true
    }

    static mapping = {
        autoTimestamp true
    }

}
