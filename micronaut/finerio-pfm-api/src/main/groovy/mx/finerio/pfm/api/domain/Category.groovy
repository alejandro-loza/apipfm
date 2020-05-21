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
    Integer parentCategoryId
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    Category(CategoryCommand cmd, User user) {
        this.user = user
        this.name = cmd.name
        this.color = cmd.color
        this.parentCategoryId = cmd.parentCategoryId
    }

    Category(){}

    static constraints = {
        user nullable: false
        name nullable: false, blank:false
        color  nullable: false, blank:false
        parentCategoryId nullable: false
        dateDeleted nullable:true
    }

    static mapping = {
        autoTimestamp true
    }

}
