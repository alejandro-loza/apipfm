package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.imp.TransactionServiceImp
import mx.finerio.pfm.api.validation.TransactionCommand
import spock.lang.Specification

class TransactionServiceSpec extends Specification {

    TransactionService transactionService = new TransactionServiceImp()

    void setup(){
        transactionService.transactionGormService = Mock(TransactionGormService)
        transactionService.accountService = Mock(AccountService)
    }

    def 'Should save an transaction'(){
        given:'a transaction command request body'
        TransactionCommand cmd = new TransactionCommand()
        cmd.with {
            accountId = 666
            date =  new Date().getTime()
        }
        when:
        1 * transactionService.accountService.getAccount(_ as Long)
        1 * transactionService.transactionGormService.save(_  as Transaction) >> new Transaction()

        def response = transactionService.create(cmd)

        then:
        response instanceof Transaction
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
        NotFoundException e = thrown()
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
