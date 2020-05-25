package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.imp.AccountServiceImp
import mx.finerio.pfm.api.validation.AccountCommand
import spock.lang.Specification

class AccountServiceSpec extends Specification {

    AccountService accountService = new AccountServiceImp()

    void setup(){
        accountService.accountGormService = Mock(AccountGormService)
        accountService.userService = Mock(UserService)
        accountService.financialEntityService = Mock(FinancialEntityService)
    }

    def 'Should save an account'(){
        given:'a account command request body'
        AccountCommand cmd = new AccountCommand()
        cmd.with {
            userId = 111
            financialEntityId = 666
            nature = 'No test'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        when:
        1 * accountService.userService.getUser(_ as Long) >> new User()
        1 * accountService.financialEntityService.getById(_ as Long) >> new FinancialEntity()
        1 * accountService.accountGormService.save(_  as Account) >> new Account()

        def response = accountService.create(cmd)

        then:
        response instanceof Account
    }

    def "Should throw exception on null body"() {

        when:
        accountService.create(null)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should get an account"(){


        when:
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> generateAccount()

        def result = accountService.getAccount(1L)

        then:
        result instanceof Account
    }

    def "Should not get a account and throw exception"(){

        when:
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        accountService.getAccount(666)

        then:
        NotFoundException e = thrown()
        e.message == 'The account ID you requested was not found.'
    }

    def "Should get all accounts" () {
        when:
        1 * accountService.accountGormService.findAllByDateDeletedIsNull(_ as Map) >> [generateAccount()]
        def response = accountService.getAll()

        then:
        assert response instanceof  List<Account>
    }

    def "Should not get all user" () {
        when:
        1 * accountService.accountGormService.findAllByDateDeletedIsNull(_ as Map) >> []
        def response = accountService.getAll()

        then:
        response instanceof  List<AccountDto>
        response.isEmpty()
    }

    def "Should get account by a cursor " () {

        when:
        1 * accountService.accountGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(_ as Long, _ as Map) >> [generateAccount()]
        def response = accountService.findAllByCursor(2)

        then:
        response instanceof  List<AccountDto>
    }

    private static Account generateAccount() {
        def account = new Account()
        account.user = new User(id: 1)
        account.financialEntity = new FinancialEntity(id: 1)
        account
    }

}
