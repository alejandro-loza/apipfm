package mx.finerio.pfm.api.dtos

import mx.finerio.pfm.api.domain.FinancialEntity

class FinancialEntityDto {

    Long id
    String name
    String code
    Date dateCreated
    Date lastUpdated

    FinancialEntityDto(FinancialEntity financialEntity) {
        this.id = financialEntity.id
        this.name = financialEntity.name
        this.code = financialEntity.code
        this.dateCreated = financialEntity.dateCreated
        this.lastUpdated = financialEntity.lastUpdated
    }

    FinancialEntityDto() {}
}
