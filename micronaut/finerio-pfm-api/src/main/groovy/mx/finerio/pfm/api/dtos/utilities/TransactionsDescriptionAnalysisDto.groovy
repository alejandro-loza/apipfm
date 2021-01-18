package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class TransactionsDescriptionAnalysisDto {
    String description
    float average
    int quantity
    float amount
}
