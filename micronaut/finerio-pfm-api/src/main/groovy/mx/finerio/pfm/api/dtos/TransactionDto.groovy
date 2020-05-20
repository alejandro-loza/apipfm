package mx.finerio.pfm.api.dtos

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction

class TransactionDto extends ResourceDto{
    Long accountId
    Date date
    boolean charge
    String description
    float  amount

    TransactionDto() {}

    TransactionDto(Transaction transaction) {
        this.id = transaction.id
        this.accountId = transaction.account.id
        this.date = transaction.date
        this.charge = transaction.charge
        this.description = transaction.description
        this.amount = transaction.amount
        this.dateCreated = transaction.dateCreated
        this.lastUpdated = transaction.lastUpdated
    }

}
