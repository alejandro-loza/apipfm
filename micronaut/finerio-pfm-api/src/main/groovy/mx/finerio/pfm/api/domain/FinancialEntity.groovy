package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.validation.FinancialEntityCommand
import org.grails.datastore.gorm.GormEntity

@Entity
class FinancialEntity implements GormEntity<FinancialEntity> {
    Long id
    String name
    String code
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    FinancialEntity (){}

    FinancialEntity(FinancialEntityCommand cmd){
        this.name = cmd.name
        this.code = cmd.code
    }

    static constraints = {
        name nullable: false, blank:false
        code nullable: false, blank:false
        dateDeleted nullable:true
    }

    static mapping = {
        autoTimestamp true
    }
}
