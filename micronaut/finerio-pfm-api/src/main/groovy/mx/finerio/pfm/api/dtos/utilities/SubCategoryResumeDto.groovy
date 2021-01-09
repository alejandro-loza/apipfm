package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class SubCategoryResumeDto extends BaseCategoryResumeDto {
    List<TransactionsByDateDto> transactionsByDate
}
