package mx.finerio.pfm.api.dtos.resource

import groovy.transform.ToString
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.resource.ResourceDto

@ToString(includeNames = true, includePackage = false)
class FinancialEntityDto extends ResourceDto{

    String name
    String code

    FinancialEntityDto(FinancialEntity financialEntity) {
        this.id = financialEntity.id
        this.name = financialEntity.name
        this.code = financialEntity.code
        this.dateCreated = financialEntity.dateCreated
        this.lastUpdated = financialEntity.lastUpdated
    }

    FinancialEntityDto() {}
}
