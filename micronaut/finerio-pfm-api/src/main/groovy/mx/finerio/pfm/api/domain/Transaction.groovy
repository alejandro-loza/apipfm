package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false,
    includes = 'id, executionDate, charge, description, amount')
class Transaction implements GormEntity<Transaction> {
    Long id
    Account account
    Date executionDate
    boolean charge
    String description
    float  amount
    Date dateCreated
    Date lastUpdated
    Date dateDeleted
    Category category
    SystemCategory systemCategory

    Transaction(){}

    Transaction(TransactionCreateCommand cmd, Account account){
        this.account = account
        this.executionDate = new Date(cmd.date)
        this.charge = cmd.charge
        this.description = cmd.description
        this.amount = cmd.amount.setScale(
                2, BigDecimal.ROUND_HALF_UP )

    }

    static constraints = {
        id generator: 'native', params:[sequence:'TRANSACTION_SEQ']
        executionDate nullable: false, blank:false
        description  nullable: false, blank:false
        amount nullable: false
        dateDeleted nullable:true
        category nullable:true
        systemCategory nullable:true
    }

    static mapping = {
        autoTimestamp true
    }
}
