package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand

interface BudgetService {
    Budget create(BudgetCreateCommand cmd)
    Budget find(Long id)
    Budget update(BudgetUpdateCommand cmd, Long id)
    void delete(Long id)
    List<BudgetDto> getAll()
    List<BudgetDto> findAllByCursor(Long cursor)
}