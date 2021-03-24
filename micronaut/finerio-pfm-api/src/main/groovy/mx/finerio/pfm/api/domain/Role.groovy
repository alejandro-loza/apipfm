package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false)
class Role implements GormEntity<Role> {

    String authority

    static constraints = {
        id generator: 'native', params:[sequence:'ROLE_SEQ']
        authority nullable: false, unique: true
    }

}
