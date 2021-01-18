package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.AccountDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.AccountUpdateCommand

interface AccountService {

    @Log
    Account create(AccountCreateCommand cmd)

    @Log
    Account getAccount(Long id)

    @Log
    Account update(AccountUpdateCommand cmd, Long id)

    @Log
    void updateBalanceByTransaction(Transaction transaction)

    @Log
    void delete(Account account)

    @Log
    List<AccountDto> findAllByUserAndCursor(Long userId, Long cursor)

    @Log
    List<AccountDto> findAllAccountDtosByUser(Long userId)

    @Log
    List<Account> findAllByUserId(Long userId)

    @Log
    List<Account> findAllByUserBoundedByMaxRows(User user)

    @Log
    List<Account> findAllByUser(User user)

    @Log
    List<Account> findAllByFinancialEntity(FinancialEntity financialEntity)

}
