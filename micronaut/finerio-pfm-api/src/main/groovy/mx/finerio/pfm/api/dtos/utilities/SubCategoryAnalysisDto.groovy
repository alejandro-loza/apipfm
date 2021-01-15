package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString
import mx.finerio.pfm.api.dtos.resource.TransactionDto

@ToString(includeNames = true, includePackage = false)
class SubCategoryAnalysisDto extends BaseCategoryResumeDto {
    float average
    Integer quantity = 0
    List<TransactionDto> transactionsByDate
}
