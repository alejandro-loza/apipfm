package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.security.utils.SecurityService
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.AccountDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.imp.AccountServiceImp
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.AccountUpdateCommand
import spock.lang.Specification

import java.security.Principal

import static java.util.Optional.of

@Property(name = 'spec.name', value = 'account service')
@MicronautTest(application = Application.class)
class AccountServiceSpec extends Specification {

    AccountService accountService = new AccountServiceImp()

    void setup(){
        accountService.accountGormService = Mock(AccountGormService)
        accountService.userService = Mock(UserService)
        accountService.financialEntityService = Mock(FinancialEntityService)
        accountService.securityService = Mock(SecurityService)
        accountService.clientService = Mock(ClientService)
    }

    def 'Should save an account'(){
        given:'a account command request body'
        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = 111
            financialEntityId = 666
            nature = 'Debit'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        and:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.findUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client
        1 * accountService.financialEntityService.getById(_ as Long) >> new FinancialEntity()
        1 * accountService.accountGormService.save(_  as Account) >> account

        def response = accountService.create(cmd)

        then:
        response instanceof Account
    }

    def 'Should not save an account and throw not found user client users not match '(){
        given:'a account command request body'
        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = 111
            financialEntityId = 666
            nature = 'Debit'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        and:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.findUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  new Client()
        0 * accountService.financialEntityService.getById(_ as Long) >> new FinancialEntity()
        0 * accountService.accountGormService.save(_  as Account) >> account

        accountService.create(cmd)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should throw exception on null body"() {

        when:
        accountService.create(null)
        then:
        BadRequestException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should get an account"(){
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> account
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client

        def result = accountService.getAccount(1L)
        then:
        result instanceof AccountDto
    }

    def "Should throw not found account on a different query client "(){
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> account
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  new Client()

        accountService.getAccount(1L)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should not get a account and throw exception"(){

        when:
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        accountService.getAccount(666)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should get all accounts by user" () {
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.findUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client
        1 * accountService.accountGormService.findAllByUserAndDateDeletedIsNull(_ as User, _ as Map) >> [account]
        def response = accountService.findAllAccountDtosByUser(account.user.id)

        then:
        assert response instanceof  List<Account>
        assert response.first().id == account.id
    }

    def "Should throw not found exception on get all accounts by user with wrong client " () {
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.findUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  new Client()
        0 * accountService.accountGormService.findAllByUserAndDateDeletedIsNull(_ as User, _ as Map)

        accountService.findAllAccountDtosByUser(account.user.id)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should get all accounts by user and cursor" () {
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.findUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client
        1 * accountService.accountGormService
                .findAllByUserAndDateDeletedIsNullAndIdLessThanEquals(_ as User, _ as Long, _ as Map) >> [account]
        def response = accountService.findAllByUserAndCursor(account.user.id, 2)

        then:
        assert response instanceof  List<Account>
        assert response.first().id == account.id
    }

    def "Should update an account"(){
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        and:
        AccountUpdateCommand cmd = new AccountUpdateCommand()
        cmd.with {
            userId = account.user.id
            financialEntityId = 666
            nature = 'No test'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        def entity = new FinancialEntity()
        entity.with {
            name = 'name'
            code = 'code'
        }

        when:
        1 * accountService.userService.findUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> account
        1 * accountService.financialEntityService.getById(_ as Long) >> entity
        1 * accountService.accountGormService.save(_  as Account) >> account

        def response = accountService.update(cmd, account.user.id)

        then:
        assert response instanceof  Account

    }

    def "Should partially update an account"(){
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        and:
        AccountUpdateCommand cmd = new AccountUpdateCommand()
        cmd.with {
            userId = account.user.id
            financialEntityId = 666
        }

        def entity = new FinancialEntity()
        entity.with {
            name = 'name'
            code = 'code'
        }

        when:
        1 * accountService.userService.findUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> account
        1 * accountService.financialEntityService.getById(_ as Long) >> entity
        1 * accountService.accountGormService.save(_  as Account) >> account

        def response = accountService.update(cmd, account.user.id)

        then:
        assert response instanceof  Account

    }


    def "Should throw not found exception update an account with different client of the user"(){
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        and:
        AccountUpdateCommand cmd = new AccountUpdateCommand()
        cmd.with {
            userId = account.user.id
            financialEntityId = 666
            nature = 'No test'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        def entity = new FinancialEntity()
        entity.with {
            name = 'name'
            code = 'code'
        }

        when:
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  new Client()
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> account
        0 * accountService.accountGormService.save(_  as Account) >> account

        accountService.update(cmd, account.user.id)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should delete an account"(){
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        and:
        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = account.user.id
            financialEntityId = 666
            nature = 'No test'
            name = 'no test'
            number = 1234123412341234
            balance = 1000.00
        }

        def entity = new FinancialEntity()
        entity.with {
            name = 'name'
            code = 'code'
        }

        when:

        1 * accountService.accountGormService.save(_  as Account) >> account

        def response = accountService.delete(account)

        then:
        assert response == null

    }

    private static Account generateAccount(Client client) {
        Account account = new Account()
        account.user = new User(id: 1, client: client)
        account.financialEntity = new FinancialEntity(id: 1, name: 'wakanda bank', code: 'WAKANDABC')
        account
    }

    private static Client generateClient() {
        Client client =new Client()
        client.with {
            id = 666
            username = 'awesome client'
        }
        client
    }

}
