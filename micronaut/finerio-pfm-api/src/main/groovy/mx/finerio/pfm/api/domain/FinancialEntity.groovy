package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import mx.finerio.pfm.api.validation.FinancialEntityCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false,
    includes = 'id, name, code')
class FinancialEntity implements GormEntity<FinancialEntity> {
    Long id
    String name
    String code
    Client client
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    FinancialEntity (){}

    FinancialEntity(FinancialEntityCreateCommand cmd, Client client){
        this.name = cmd.name
        this.code = cmd.code
        this.client = client
    }

    static constraints = {
		id generator: 'native', params:[sequence:'FINANCIAL_ENTITY_SEQ']
        name nullable: false, blank:false
        code nullable: false, blank:false
        dateDeleted nullable:true
    }

    static mapping = {
        autoTimestamp true
    }
}
