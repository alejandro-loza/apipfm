package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
class Category extends CategoryModel implements GormEntity<Category> {

    Long id
    User user
    Category parent
    Client client
    Date dateDeleted

    Category(CategoryCreateCommand cmd,  Client client) {
        this.name = cmd.name
        this.color = cmd.color
        this.client = client
    }

    Category(){}

    static constraints = {
        user nullable: true
        parent nullable:true
    }

}
