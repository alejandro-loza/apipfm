package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.imp.TransactionServiceImp
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import spock.lang.Specification

@Property(name = 'spec.name', value = 'transaction service')
@MicronautTest(application = Application.class)
class TransactionServiceSpec extends Specification {

    TransactionService transactionService = new TransactionServiceImp()

    void setup(){
        transactionService.transactionGormService = Mock(TransactionGormService)
        transactionService.accountService = Mock(AccountService)
        transactionService.categoryService = Mock(CategoryService)
    }

    def 'Should save an transaction'(){
        given:'a transaction command request body'
        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 666
            date =  new Date().getTime()
        }
        when:
        1 * transactionService.accountService.getAccount(_ as Long) >> new Account()
        1 * transactionService.transactionGormService.save(_  as Transaction) >> new Transaction()

        def response = transactionService.create(cmd)

        then:
        response instanceof Transaction
    }

    def
    'Should not save an transaction on category not found'(){
        given:'a transaction command request body'
        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 666
            date =  new Date().getTime()
            categoryId = 666
        }
        when:
        1 * transactionService.accountService.getAccount(_ as Long)
        1 * transactionService.categoryService.find(_ as Long) >> {throw new ItemNotFoundException('category.notFound') }

        def response = transactionService.create(cmd)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'category.notFound'
    }

    def "Should throw exception on null body"() {

        when:
        transactionService.create(null)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should get a transaction"(){

        when:
        1 * transactionService.transactionGormService.findByIdAndDateDeletedIsNull(_ as Long) >> new Transaction()

        def result = transactionService.find(1L)

        then:
        result instanceof Transaction
    }

    def "Should not get a transaction and throw exception"(){

        when:
        1 * transactionService.transactionGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        transactionService.find(666)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'transaction.notFound'
    }

    def "Should get all transactions" () {
        def transaction = new Transaction()
        transaction.account =  new Account(id:1)

        when:
        1 * transactionService.transactionGormService.findAllByDateDeletedIsNull(_ as Map) >> [transaction]
        def response = transactionService.getAll()

        then:
        assert response instanceof  List<Transaction>
    }

    def "Should not get all transactions" () {
        when:
        1 * transactionService.transactionGormService.findAllByDateDeletedIsNull(_ as Map) >> []
        def response = transactionService.getAll()

        then:
        response instanceof  List<TransactionDto>
        response.isEmpty()
    }

    def "Should get transaction by a cursor " () {
        given:
        def transaction = new Transaction()
        transaction.account =  new Account(id:1)

        when:
        1 * transactionService.transactionGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(_ as Long, _ as Map) >> [transaction]
        def response = transactionService.findAllByCursor(2)

        then:
        response instanceof  List<TransactionDto>
    }

}
