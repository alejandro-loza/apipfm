package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.AccountUpdateCommand

interface AccountService {
    Account create(AccountCreateCommand cmd)
    Account getAccount(Long id)
    Account update(AccountUpdateCommand cmd, Long id)
    void delete(Long id)
    List<AccountDto> findAllByUserAndCursor(Long userId, Long cursor)
    List<AccountDto> findAllAccountDtosByUser(Long userId)
    List<Account> findAllByUserId(Long userId)
}