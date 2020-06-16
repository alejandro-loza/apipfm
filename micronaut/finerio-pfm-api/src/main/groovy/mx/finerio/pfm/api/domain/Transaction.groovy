package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import mx.finerio.pfm.api.validation.TransactionCommand
import org.grails.datastore.gorm.GormEntity

@Entity
class Transaction implements GormEntity<Transaction> {
    Long id
    Account account
    Date date
    boolean charge
    String description
    float  amount
    Date dateCreated
    Date lastUpdated
    Date dateDeleted
    Category category

    Transaction(){}

    Transaction(TransactionCommand cmd, Account account){
        this.account = account
        this.date = new Date(cmd.date)
        this.charge = cmd.charge
        this.description = cmd.description
        this.amount = cmd.amount
    }

    static constraints = {
        date nullable: false, blank:false
        description  nullable: false, blank:false
        amount nullable: false
        dateDeleted nullable:true
        category nullable:true
    }

    static mapping = {
        autoTimestamp true
    }
}
