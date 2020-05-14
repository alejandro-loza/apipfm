package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.exceptions.AccountNotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.validation.AccountCommand

import javax.inject.Inject

class AccountServiceImp implements AccountService {

    public static final int MAX_ROWS = 100

    @Inject
    AccountGormService accountGormService

    @Inject
    UserService userService

    @Inject
    FinancialEntityService financialEntityService

    @Override
    Account create(AccountCommand cmd){
        accountGormService.save( new Account(cmd, userService.getUser(cmd.userId),
                        financialEntityService.getById(cmd.financialEntityId)))
    }

    @Override
    Account getAccount(Long id) {
        Optional.ofNullable(accountGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new AccountNotFoundException('The account ID you requested was not found.') })
    }

    @Override
    Account update(AccountCommand cmd, Long id){
        Account account = getAccount(id)
        account.with {
            user = userService.getUser(cmd.userId)
            financialEntity = financialEntityService.getById(cmd.financialEntityId)
            nature = cmd.nature
            name = cmd.name
            number = Long.valueOf(cmd.number)
            balance = cmd.balance
        }
        accountGormService.save(account)
    }

    @Override
    void delete(Long id){
        Account account = getAccount(id)
        account.dateDeleted = new Date()
        accountGormService.save(account)
    }

    @Override
    List<AccountDto> getAll() {
        accountGormService.findAll([max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new AccountDto(it)}
    }

    @Override
    List<AccountDto> findAllByCursor(Long cursor) {
        accountGormService.findByIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new AccountDto(it)}
    }

}
