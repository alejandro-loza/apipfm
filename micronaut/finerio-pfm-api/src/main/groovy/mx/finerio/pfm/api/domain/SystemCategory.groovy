package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
class SystemCategory  extends CategoryModel implements GormEntity<SystemCategory> {

    SystemCategory parent

    static constraints = {
        parent nullable:true
    }

}
