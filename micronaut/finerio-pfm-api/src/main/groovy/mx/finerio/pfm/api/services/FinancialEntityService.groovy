package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.validation.FinancialEntityCommand

interface FinancialEntityService {
    FinancialEntity create(FinancialEntityCommand cmd)
    FinancialEntity getById(Long id)
}