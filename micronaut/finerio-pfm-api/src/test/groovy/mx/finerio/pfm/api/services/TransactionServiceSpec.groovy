package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.CategorizerDto
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.exceptions.BadRequestException
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
        transactionService.categoryService = Mock(CategoryService)
        transactionService.transactionGormService = Mock(TransactionGormService)
        transactionService.accountService = Mock(AccountService)
        transactionService.categorizerService = Mock(CategorizerService)
        transactionService.systemCategoryService = Mock(SystemCategoryService)
    }

    def 'Should save an transaction'(){
        given:'a transaction command request body'
        Category category = generateCategory()

        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 666
            date =  new Date().getTime()
            categoryId = category.id
            amount = 100.50
        }

        when:

        1 * transactionService.categoryService.getById( _ as Long) >> category
        1 * transactionService.accountService.getAccount( _ as Long) >> new Account()
        1 * transactionService.transactionGormService.save( _  as Transaction) >> new Transaction()

        def response = transactionService.create(cmd)

        then:
        response instanceof Transaction
    }

    def 'Should save an transaction with no category and use categorizer to set it'() {
        given:'a transaction command request body'

        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 666
            date =  new Date().getTime()
            amount = 100.50
        }

        CategorizerDto categorizerDto = new CategorizerDto()
        categorizerDto.with {
            categoryId = 'uuid'
        }

        when:

        1 * transactionService.accountService.getAccount(_ as Long) >> new Account()
        1 * transactionService.categorizerService.searchCategory(_ ) >> categorizerDto
        1 * transactionService.systemCategoryService.findByFinerioConnectId(_ as String) >> new SystemCategory()
        1 * transactionService.transactionGormService.save( _  as Transaction) >> new Transaction()

        def response = transactionService.create(cmd)

        then:
        response instanceof Transaction
    }

    def 'Should save an transaction with  category null on categorizer  responses empty '(){
        given:'a transaction command request body'

        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 666
            date =  new Date().getTime()
            amount = 100.50
        }


        when:

        1 * transactionService.accountService.getAccount(_ as Long) >> new Account()
        1 * transactionService.categorizerService.searchCategory(_ ) >> new CategorizerDto()
        0 * transactionService.categoryService.getById(_ as Long)
        1 * transactionService.transactionGormService.save( _  as Transaction) >> new Transaction()


        def response = transactionService.create(cmd)

        then:
        response instanceof Transaction
    }

    def 'Should not save an transaction on no parent category'(){
        given:'a transaction command request body'
        Category category = new Category()
        category.with {
            name: 'sub category'
            category.id = 666
        }
        category.parent = null

        def account = new Account()
        account.id = 666
        def transaction = new Transaction()
        transaction.account =  account

        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId =  account.id
            date =  new Date().getTime()
            categoryId = category.id
            amount = 100.50
        }

        when:

        1 * transactionService.categoryService.getById( _ as Long) >> category
        1 * transactionService.accountService.getAccount(_ as Long)
        0 * transactionService.transactionGormService.save(_  as Transaction)

        transactionService.create(cmd)

        then:
        BadRequestException e = thrown()
        e.message == 'category.parentCategory.null'
    }

    def 'Should not save an transaction on category not found'(){
        given:'a transaction command request body'
        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 666
            date =  new Date().getTime()
            categoryId = 666
            amount = 100.50
        }
        when:
        1 * transactionService.categoryService.getById(_ as Long) >> {throw new ItemNotFoundException('category.notFound') }
        1 * transactionService.accountService.getAccount(_ as Long)
        0 * transactionService.transactionGormService.save(_  as Transaction)

        transactionService.create(cmd)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'category.notFound'
    }

    def 'Should not save an transaction on no system parent category'(){
        given:'a transaction command request body'
        SystemCategory category = new SystemCategory()
        category.with {
            name: 'sub category'
            category.id = 666
        }
        category.parent = null

        def account = new Account()
        account.id = 666
        def transaction = new Transaction()
        transaction.account =  account

        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId =  account.id
            date =  new Date().getTime()
            categoryId = category.id
            amount = 100.50
        }

        when:

        1 * transactionService.systemCategoryService.find( _ as Long) >> category
        1 * transactionService.accountService.getAccount(_ as Long)
        0 * transactionService.transactionGormService.save(_  as Transaction)

        transactionService.create(cmd)

        then:
        BadRequestException e = thrown()
        e.message == 'category.parentCategory.null'
    }

    def "Should throw exception on null body"() {

        when:
        transactionService.create(null)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
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

    private static Category generateCategory() {
        Category category1 = new Category()
        category1.with {
            name: 'sub category'
            category1.id = 666
        }


        Category category = new Category()
        category.with {
            parent = category1
            name = 'parent'
            category.id = 333
        }
        category
    }


}
