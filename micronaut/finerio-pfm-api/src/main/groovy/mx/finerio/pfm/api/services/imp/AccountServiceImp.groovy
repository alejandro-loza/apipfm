package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.AccountUpdateCommand

import javax.inject.Inject

class AccountServiceImp extends ServiceTemplate implements AccountService {

    @Inject
    AccountGormService accountGormService

    @Inject
    UserService userService

    @Inject
    FinancialEntityService financialEntityService

    @Override
    Account create(AccountCreateCommand cmd){
        verifyBody(cmd)
        User user = userService.getUser(cmd.userId)
        verifyLoggedClient(user.client)
        accountGormService.save( new Account(cmd, user,
                        financialEntityService.getById(cmd.financialEntityId)))
    }

    @Override
    Account getAccount(Long id) {
        Account account = Optional.ofNullable(accountGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('account.notFound') })
        verifyLoggedClient(account.user.client)
        account
    }

    @Override
    Account update(AccountUpdateCommand cmd, Long id){
        verifyBody(cmd)
        Account account = getAccount(id)
        account.with {
            user = cmd.userId ? userService.getUser(cmd.userId) : account.user
            financialEntity = cmd.financialEntityId ?
                    financialEntityService.getById(cmd.financialEntityId)
                    : account.financialEntity
            nature = cmd.nature ?: account.nature
            name = cmd.name ?: account.name
            number = cmd.number ?: account.number
            balance = cmd.balance ?: account.balance
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
    List<AccountDto> findAllByUserAndCursor(Long userId, Long cursor) {
        User user = userService.getUser(userId)
        verifyLoggedClient(user.client)
        accountGormService.findAllByUserAndDateDeletedIsNullAndIdLessThanEquals(
                user,cursor,[max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new AccountDto(it)}
    }

    @Override
    List<AccountDto> findAllAccountDtosByUser(Long userId) {
        findAllByUserId(userId).collect { new AccountDto(it) }
    }

    @Override
    List<Account> findAllByUserId(Long userId) {
        User user = userService.getUser(userId)
        verifyLoggedClient(user.client)
        accountGormService.findAllByUserAndDateDeletedIsNull(
                user, [max: MAX_ROWS, sort: 'id', order: 'desc'])
    }

    private void verifyLoggedClient(Client client) {
        if (client.id != getCurrentLoggedClient().id) {
            throw new ItemNotFoundException('account.notFound')
        }
    }

}
