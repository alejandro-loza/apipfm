package mx.finerio.pfm.api.domain

import mx.finerio.pfm.api.validation.TransactionCommand

class Transaction {
    Long id
    Date date
    boolean charge
    String description
    float  amount
    Date dateCreated
    Date lastUpdated

    Transaction(){}

    Transaction(TransactionCommand cmd){
        this.date = cmd.date
        this.charge = cmd.charge
        this.description = cmd.description
        this.amount = cmd.amount
    }

    static constraints = {
        date nullable: false, blank:false
        description  nullable: false, blank:false
        amount nullable: false
    }

    static mapping = {
        autoTimestamp true
    }
}
