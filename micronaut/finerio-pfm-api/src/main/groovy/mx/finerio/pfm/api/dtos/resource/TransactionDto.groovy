package mx.finerio.pfm.api.dtos.resource

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString
import mx.finerio.pfm.api.domain.Transaction

@JsonInclude(JsonInclude.Include.ALWAYS)
@ToString(includeNames = true, includePackage = false,
    includeSuperProperties = true)
class TransactionDto extends ResourceDto{
    Date date
    boolean charge
    String description
    BigDecimal  amount
    Long categoryId
    TransactionDto() {}

    TransactionDto(Transaction transaction) {
        this.id = transaction.id
        this.date = transaction.executionDate
        this.charge = transaction.charge
        this.description = transaction.description
        this.amount =  new BigDecimal(transaction.amount).setScale(2,BigDecimal.ROUND_HALF_UP)
        this.dateCreated = transaction.dateCreated
        this.lastUpdated = transaction.lastUpdated
        this.categoryId = transaction?.category?.id
    }

}
