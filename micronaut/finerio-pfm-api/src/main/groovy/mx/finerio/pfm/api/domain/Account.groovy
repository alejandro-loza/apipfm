package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

@Entity
class Account  implements GormEntity<Account> {

    Long id
    User user
    Long financialEntityId
    String nature
    String name
    Long number
    float balance
    Date dateCreated
    Date lastUpdated

    static constraints = {
        name nullable: false, blank:false
        financialEntityId nullable: false
        nature  nullable: false, blank:false
        number nullable: false, blank:false
        balance nullable: false, blank:false
    }

    static mapping = {
        autoTimestamp true
    }

}
