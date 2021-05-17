package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.*
import mx.finerio.pfm.api.dtos.resource.AccountDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.AccountUpdateCommand
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service
class AccountServiceImp extends ServiceTemplate implements AccountService {

    @Inject
    AccountGormService accountGormService

    @Inject
    UserService userService

    @Inject
    FinancialEntityService financialEntityService

    @Override
    @Transactional
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
            financialEntity = cmd.financialEntityId
                    ? financialEntityService.getById(cmd.financialEntityId)
                    : account.financialEntity
            nature = cmd.nature ?: account.nature
            name = cmd.name ?: account.name
            cardNumber = cmd.number ?: account.cardNumber
            balance = cmd.balance ?: account.balance
            chargeable = cmd.chargeable != null  ? cmd.chargeable : account.chargeable
        }
        accountGormService.save(account)
    }

    @Override
    void updateBalanceByTransaction(Transaction transaction) {
        Account account = transaction.account
        if(account.chargeable){
            account.balance = (float) (transaction.charge
                    ? account.balance - transaction.amount
                    : account.balance + transaction.amount)
        }
        accountGormService.save(account)
    }

    @Override
    @Transactional
    void delete(Account account){
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
        findAllByUserBoundedByMaxRows(user)
    }

    @Override
    @Transactional
    List<Account> findAllByUserBoundedByMaxRows(User user) {
        verifyLoggedClient(user.client)
        accountGormService.findAllByUserAndDateDeletedIsNull(
                user, [max: MAX_ROWS, sort: 'id', order: 'desc'])
    }

    @Override
    @Transactional
    List<Account> findAllByUser(User user) {
        verifyLoggedClient(user.client)
        accountGormService.findAllByUserAndDateDeletedIsNull(user, [ sort: 'id', order: 'desc'])
    }

    @Override
    List<Account> findAllByFinancialEntity(FinancialEntity financialEntity) {
        accountGormService.findAllByFinancialEntityAndDateDeletedIsNull(financialEntity)
    }

    private void verifyLoggedClient(Client client) {
        if (client.id != getCurrentLoggedClient()?.id) {
            throw new ItemNotFoundException('account.notFound')
        }
    }

}
