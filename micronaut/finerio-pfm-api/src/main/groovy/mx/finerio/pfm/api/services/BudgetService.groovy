package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.validation.BudgetCommand

interface BudgetService {
    Budget create(BudgetCommand cmd)
    Budget find(Long id)
    Budget update(BudgetCommand cmd, Long id)
    void delete(Long id)
    List<BudgetDto> getAll()
    List<BudgetDto> findAllByCursor(Long cursor)
}