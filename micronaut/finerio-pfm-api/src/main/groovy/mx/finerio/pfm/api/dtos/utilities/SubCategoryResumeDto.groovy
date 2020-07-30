package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class SubCategoryResumeDto {
    Long categoryId
    float amount
    List<TransactionsByDateDto> transactionsByDate
}
