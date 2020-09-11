package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false,
    includes = 'id, name, color')
class SystemCategory implements GormEntity<SystemCategory> {

    Long id
    String name
    String color
    SystemCategory parent
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    static constraints = {
        name nullable: false, blank:false
        color  nullable: true, blank:false
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
