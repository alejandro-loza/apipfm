package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

interface BudgetService {

    @Log
    void verifyBody(ValidationCommand cmd)

    @Log
    BudgetDto create(BudgetCreateCommand cmd)

    @Log
    Budget find(Long id)

    @Log
    BudgetDto get(Long id)

    @Log
    BudgetDto update(BudgetUpdateCommand cmd, Budget budget)

    @Log
    void delete(Long id)

    @Log
    List<BudgetDto> getAll()

    @Log
    List<BudgetDto> findAllByUserAndCursor(Long userId, Long cursor)

    @Log
    List<BudgetDto> findAllByUserId(Long userId)

    @Log
    List<BudgetDto>findAllByUser(User user)

    @Log
    Budget findByUserAndCategory(User user, Category category)

    @Log
    Budget findByCategory(Category category)

}
