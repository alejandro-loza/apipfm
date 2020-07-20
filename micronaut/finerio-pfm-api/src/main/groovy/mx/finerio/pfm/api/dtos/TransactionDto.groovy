package mx.finerio.pfm.api.dtos

import groovy.transform.ToString

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction

@ToString(includeNames = true, includePackage = false,
    includeSuperProperties = true)
class TransactionDto extends ResourceDto{
    Long accountId
    Date date
    boolean charge
    String description
    float  amount
    Long categoryId
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
        this.categoryId = transaction?.category?.id
    }

}
