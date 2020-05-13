package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.FinancialEntity

@Service(FinancialEntity)
interface FinancialEntityGormService {
    FinancialEntity save(FinancialEntity account)
    FinancialEntity findByIdAndDateDeletedIsNull(Long id)
}