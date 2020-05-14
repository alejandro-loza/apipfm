package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.validation.FinancialEntityCommand

interface FinancialEntityService {
    FinancialEntity create(FinancialEntityCommand cmd)
    FinancialEntity getById(Long id)
    FinancialEntity update(FinancialEntityCommand cmd, Long id)
    List<FinancialEntity> getAll()
    List<FinancialEntity> findAllByCursor(Long cursor)
    void delete(Long id)
}