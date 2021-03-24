package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false,
    includes = 'id, name, amount')
class Budget implements GormEntity<Budget> {

    Long id
    User user
    Category category
    SystemCategory systemCategory
    String name
    float amount
    Date dateCreated
    Date lastUpdated
    Date dateDeleted
    BigDecimal warningPercentage

    Budget(){}

    static constraints = {
        id generator: 'native', params:[sequence:'BUDGET_SEQ']
        name nullable: false, blank:false
        user nullable: false
        dateDeleted nullable:true
        systemCategory nullable:true
        category nullable:true
        warningPercentage nullable: false
    }

    static mapping = {
        autoTimestamp true
    }

}
