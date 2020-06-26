package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.validation.FinancialEntityCreateCommand
import mx.finerio.pfm.api.validation.FinancialEntityUpdateCommand

interface FinancialEntityService {
    FinancialEntity create(FinancialEntityCreateCommand cmd)
    FinancialEntity getById(Long id)
    FinancialEntity update(FinancialEntityUpdateCommand cmd, Long id)
    List<FinancialEntityDto> getAll()
    List<FinancialEntityDto> findAllByCursor(Long cursor)
    void delete(Long id)
}