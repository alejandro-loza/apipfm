package mx.finerio.pfm.api.dtos

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class TransactionsByDateDto {
    Long date
    List<TransactionDto> transactions
}
