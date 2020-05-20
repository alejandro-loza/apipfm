package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.validation.AccountCommand
import mx.finerio.pfm.api.validation.CategoryCommand
import org.grails.datastore.gorm.GormEntity

import java.awt.Color

@Entity
class Category implements GormEntity<Category> {

    Long id
    User user
    String name
    Color color
    Integer parentCategoryId //todo is this another class?
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    Category(CategoryCommand cmd, User user) {
        this.user = user
        this.name = cmd.name
        this.color = new Color(cmd.color)
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
