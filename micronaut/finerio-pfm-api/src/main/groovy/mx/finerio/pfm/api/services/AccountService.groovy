package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.AccountDto
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
    void delete(Long id)

    @Log
    List<AccountDto> findAllByUserAndCursor(Long userId, Long cursor)

    @Log
    List<AccountDto> findAllAccountDtosByUser(Long userId)

    @Log
    List<Account> findAllByUserId(Long userId)

}
