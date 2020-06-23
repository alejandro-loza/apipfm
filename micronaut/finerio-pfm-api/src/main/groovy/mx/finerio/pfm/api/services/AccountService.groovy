package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.validation.AccountCommand

interface AccountService {
    Account create(AccountCommand cmd)
    Account getAccount(Long id)
    Account update(AccountCommand cmd, Long id)
    void delete(Long id)
    List<AccountDto> getAll()
    List<AccountDto> findAllByCursor(Long cursor)
    List<AccountDto> findAllByUserAndCursor(Long userid, Long cursor)
    List<AccountDto> findAllByUser(Long userid)
}