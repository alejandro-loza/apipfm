package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.FinancialEntityCreateCommand
import mx.finerio.pfm.api.validation.FinancialEntityUpdateCommand

interface FinancialEntityService {

    @Log
    FinancialEntity create(FinancialEntityCreateCommand cmd)

    @Log
    FinancialEntity getById(Long id)

    @Log
    FinancialEntity update(FinancialEntityUpdateCommand cmd, Long id)

    @Log
    List<FinancialEntityDto> getAll()

    @Log
    List<FinancialEntityDto> findAllByCursor(Long cursor)

    @Log
    void delete(Long id)

}
