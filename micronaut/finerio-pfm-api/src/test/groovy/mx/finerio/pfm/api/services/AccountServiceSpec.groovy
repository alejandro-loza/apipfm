package mx.finerio.pfm.api.services

import io.micronaut.security.authentication.Authentication
import io.micronaut.security.utils.SecurityService
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.imp.AccountServiceImp
import mx.finerio.pfm.api.validation.AccountCommand
import spock.lang.Specification

import java.security.Principal

import static java.util.Optional.of

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
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> account
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client

        def result = accountService.getAccount(1L)
        then:
        result instanceof Account
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
        NotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should not get a account and throw exception"(){

        when:
        1 * accountService.accountGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        accountService.getAccount(666)

        then:
        NotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should get all accounts" () {
        when:
        1 * accountService.accountGormService.findAllByDateDeletedIsNull(_ as Map) >> [generateAccount()]
        def response = accountService.getAll()

        then:
        assert response instanceof  List<Account>
    }

    def "Should get all accounts by user" () {
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.getUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client
        1 * accountService.accountGormService.findAllByUserAndDateDeletedIsNull(_ as User, _ as Map) >> [account]
        def response = accountService.findAllByUser(account.user.id)

        then:
        assert response instanceof  List<Account>
        assert response.first().id == account.id
    }

    def "Should throw not found exception on get all accounts by user with wrong client " () {
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.getUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  new Client()
        0 * accountService.accountGormService.findAllByUserAndDateDeletedIsNull(_ as User, _ as Map)

        accountService.findAllByUser(account.user.id)

        then:
        NotFoundException e = thrown()
        e.message == 'account.notFound'
    }

    def "Should get all accounts by user and cursor" () {
        given:
        Client client = generateClient()
        Account account = generateAccount(client)

        when:
        1 * accountService.userService.getUser(_ as Long) >> account.user
        1 * accountService.securityService.getAuthentication() >> of(Principal)
        1 * accountService.clientService.findByUsername(_ as String) >>  client
        1 * accountService.accountGormService
                .findAllByUserAndDateDeletedIsNullAndIdLessThanEquals(_ as User, _ as Long, _ as Map) >> [account]
        def response = accountService.findAllByUserAndCursor(account.user.id, 2)

        then:
        assert response instanceof  List<Account>
        assert response.first().id == account.id
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
            password = 'awesome password'
        }
        client
    }

}
