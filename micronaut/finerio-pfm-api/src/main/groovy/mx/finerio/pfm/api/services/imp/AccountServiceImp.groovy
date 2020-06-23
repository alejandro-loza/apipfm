package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.validation.AccountCommand

import javax.inject.Inject

class AccountServiceImp extends ServiceTemplate implements AccountService {

    @Inject
    AccountGormService accountGormService

    @Inject
    UserService userService

    @Inject
    FinancialEntityService financialEntityService

    @Override
    Account create(AccountCommand cmd){
        verifyBody(cmd)
        accountGormService.save( new Account(cmd, userService.getUser(cmd.userId),
                        financialEntityService.getById(cmd.financialEntityId)))
    }

    @Override
    Account getAccount(Long id) {
        Account account = Optional.ofNullable(accountGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new NotFoundException('account.notFound') })
        verifyLoggedClient(account.user.client)
        account
    }

    @Override
    Account update(AccountCommand cmd, Long id){
        verifyBody(cmd)
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
        accountGormService.findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new AccountDto(it)}
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
    List<AccountDto> findAllByUser(Long userId) {
        User user = userService.getUser(userId)
        verifyLoggedClient(user.client)
        accountGormService.findAllByUserAndDateDeletedIsNull(
                user,[max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new AccountDto(it)}
    }

    @Override
    List<AccountDto> findAllByCursor(Long cursor) {
        accountGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new AccountDto(it)}
    }

    private void verifyLoggedClient(Client client) {
        if (client != getCurrentLoggedClient()) {
            throw new NotFoundException('account.notFound')
        }
    }

}
