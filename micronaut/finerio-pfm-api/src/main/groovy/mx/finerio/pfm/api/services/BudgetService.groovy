package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand

interface BudgetService {

    @Log
    Budget create(BudgetCreateCommand cmd)

    @Log
    Budget find(Long id)

    @Log
    Budget update(BudgetUpdateCommand cmd, Long id)

    @Log
    void delete(Long id)

    @Log
    List<BudgetDto> getAll()

    @Log
    List<BudgetDto> findAllByCursor(Long cursor)

}
