package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString
import mx.finerio.pfm.api.dtos.resource.TransactionDto

@ToString(includeNames = true, includePackage = false)
class TransactionsByDateDto {
    Long date
    List<TransactionDto> transactions
}
