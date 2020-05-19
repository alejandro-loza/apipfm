package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.TransactionCommand
import spock.lang.Shared
import spock.lang.Specification
import javax.inject.Inject

@Property(name = 'spec.name', value = 'account controller')
@MicronautTest(application = Application.class)
class TransactionControllerSpec extends Specification {

    public static final String TRANSACTION_ROOT = "/transactions"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    AccountGormService accountService

    @Inject
    UserGormService userService

    @Inject
    FinancialEntityGormService financialEntityService

    @Inject
    TransactionGormService transactionGormService

    def "Should get a empty list of transactions"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET(TRANSACTION_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(TransactionDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()
    }

    def "Should create a transaction"(){
        given:'an saved Account '
        Account account1 = generateAccount()

        and:'a command request body'
        TransactionCommand cmd = new TransactionCommand()
        cmd.with {
            accountId = account1.id
            date = 1587567125458
            charge = true
            description = "UBER EATS"
            amount= 1234.56
        }

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT, cmd)

        when:
        def rsp = client.toBlocking().exchange(request, TransactionDto)

        then:
        rsp.status == HttpStatus.OK

    }

    private Account generateAccount() {
        User user1 = new User('awesome user')
        userService.save(user1)

        FinancialEntity entity = new FinancialEntity()
        entity.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        financialEntityService.save(entity)

        Account account1 = new Account()
        account1.with {
            user = user1
            financialEntity = entity
            nature = 'TEST NATURE'
            name = 'TEST NAME'
            number = 123412341234
            balance = 0.0
        }
        accountService.save(account1)
        account1
    }

    def "Should not create a transaction and throw bad request on wrong params"(){
        given:'an transaction request body with empty body'

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT,  new TransactionCommand())

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create a transaction and throw bad request on wrong body"(){
        given:'a transaction request body with empty body'

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT,  'asd')

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST
    }

    def "Should not create an transaction and throw not found exception on account not found"(){
        given:'an account request body with no found account id'

        TransactionCommand cmd = new TransactionCommand()
        cmd.with {
            accountId = 666
            date = 1587567125458
            charge = true
            description = 'UBER EATS'
            amount = 100.00
        }

        HttpRequest request = HttpRequest.POST(TRANSACTION_ROOT, cmd)

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(ErrorDto))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND
    }

    def "Should get an transaction"(){
        given:'a saved account'
        Account account1 = generateAccount()

        and:'a saved transaction'
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            charge = false
            description = 'RAPI'
        }

        transactionGormService.save(transaction)

        and:
        HttpRequest getReq = HttpRequest.GET(TRANSACTION_ROOT+"/${transaction.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, TransactionDto)

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().with {
            assert id == transaction.id
            assert accountId == transaction.accountId
            assert date == transaction.date
            assert charge == transaction.charge
            assert description == transaction.description
            assert amount == transaction.amount
        }
        !transaction.dateDeleted

    }

    private FinancialEntity getSavedFinancialEntity() {
        FinancialEntity entity = new FinancialEntity()
        entity.with {
            name = 'Gringotts'
            code = 'Gringotts Bank'
        }
        entity = financialEntityService.save(entity)
        entity
    }

    def "Should not get a transaction and throw 404"(){//TODO test the error body
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("${TRANSACTION_ROOT}/0000")

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should not get an account and throw 400"(){
        given:'a not found id request'

        HttpRequest request = HttpRequest.GET("${TRANSACTION_ROOT}/abc")

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should update an transaction"(){
        given:'a saved user'
        Account account1 = generateAccount()

        and:'a saved transaction'
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            charge = true
            description = 'rapi'
            amount = 100.00
        }
        transactionGormService.save(transaction)

        and:'an account command to update data'
        TransactionCommand cmd = generateTransactionCommand(account1)

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${TRANSACTION_ROOT}/${transaction.id}",  cmd)

        when:
        def resp = client.toBlocking().exchange(request,  Argument.of(TransactionDto) as Argument<TransactionDto>,
                Argument.of(ErrorDto))
        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            assert accountId == cmd.accountId
            assert date.getTime() == cmd.date
            assert charge == cmd.charge
            assert description == cmd.description
            assert amount == cmd.amount
        }

    }

    def "Should not update a transaction on band parameters and return Bad Request"(){
        given:'a transaction '
        Account account1 = generateAccount()

        and:'a saved transaction'
        Transaction transaction = new Transaction()
        transaction.with {
            account = account1
            date = new Date()
            charge = true
            description = 'rapi'
            amount = 100.00
        }
        transactionGormService.save(transaction)

        HttpRequest request = HttpRequest.PUT("${TRANSACTION_ROOT}/${transaction.id}",  new TransactionCommand())

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update an transaction and throw not found exception"(){
        given:
        Account account1 = generateAccount()

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${TRANSACTION_ROOT}/666",  generateTransactionCommand(account1))

        when:
        client.toBlocking().exchange(request, TransactionDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of transactions"(){

        given:'a transaction list'
        Account account1 = generateAccount()

        Transaction transaction1 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction1)
        Transaction transaction2 = new Transaction(generateTransactionCommand(account1), account1)
        transaction2.dateDeleted = new Date()
        transactionGormService.save(transaction2)
        Transaction transaction3 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction3)
        Transaction transaction4 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction4)

        and:
        HttpRequest getReq = HttpRequest.GET(TRANSACTION_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<TransactionDto> transactionDtos = body.get("data") as List<TransactionDto>
        assert !(transaction2.id in transactionDtos.id)

        assert !body.get("nextCursor")
    }

    def "Should get a list of transactions on a cursor point"(){

        given:'a transaction list'
        Account account1 = generateAccount()

        Transaction transaction1 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction1)
        Transaction transaction2 = new Transaction(generateTransactionCommand(account1), account1)
        transaction2.dateDeleted = new Date()
        transactionGormService.save(transaction2)
        Transaction transaction3 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction3)
        Transaction transaction4 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction4)

        and:
        HttpRequest getReq = HttpRequest.GET("${TRANSACTION_ROOT}?cursor=${transaction3.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<TransactionDto> transactionDtos = body.get("data") as List<TransactionDto>
        assert !(transaction2.id in transactionDtos.id)
        assert !(transaction4.id in transactionDtos.id)

        assert !body.get("nextCursor")
    }

    def "Should throw not found exception on delete no found transaction"(){
        given:
        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.DELETE("${TRANSACTION_ROOT}/${notFoundId}")

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete an transaction"() {
        given:'a transaction'
        Account account1 = generateAccount()

        Transaction transaction1 = new Transaction(generateTransactionCommand(account1), account1)
        transactionGormService.save(transaction1)

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${TRANSACTION_ROOT}/${transaction1.id}")

        when:
        def response = client.toBlocking().exchange(request, TransactionDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("${TRANSACTION_ROOT}/${transaction1.id}")

        when:
        client.toBlocking().exchange(request, Argument.of(TransactionDto) as Argument<TransactionDto>,
                Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    private TransactionCommand generateTransactionCommand(Account account1) {
        def date1 = new Date()

        TransactionCommand cmd = new TransactionCommand()
        cmd.with {
            accountId = account1.id
            date = date1.getTime()
            charge = true
            description = "UBER EATS"
            amount= 1234.56
        }
        cmd
    }


}
