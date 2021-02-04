package mx.finerio.pfm.api.services

import io.micronaut.security.utils.SecurityService
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.services.imp.BudgetServiceImp
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand
import spock.lang.Specification
import java.security.Principal

import static java.util.Optional.of

class BudgetServiceSpec extends Specification {

    BudgetService budgetService = new BudgetServiceImp()

    void setup(){
        budgetService.budgetGormService = Mock(BudgetGormService)
        budgetService.userService = Mock(UserService)
        budgetService.systemCategoryService = Mock(SystemCategoryService)
        budgetService.categoryService = Mock(CategoryService)
        budgetService.clientService = Mock(ClientService)
        budgetService.securityService = Mock(SecurityService)
        budgetService.transactionService = Mock(TransactionService)
        budgetService.accountService = Mock(AccountService)
    }

    def 'Should save an budget with category and no system category'() {

        given:'a budget command request body'
        BudgetCreateCommand cmd = generateCommand()
        def user1 = new User()
        def category1 = new Category()
        category1.id = 666
        def budget = new Budget()
        budget.with {
            user = user1
            category = category1
            name = 'test name'
            warningPercentage = 0.7
            amount = 100
        }

        when:
        1 * budgetService.userService.getUser(_ as Long) >> user1
        1 * budgetService.systemCategoryService.find(_ as Long) >> null
        1 * budgetService.categoryService.getById(_ as Long) >> category1
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget

        def response = budgetService.create(cmd)

        then:
        assert response
        response.with {
            assert amount == 100.00
            assert spent == 0.00
            assert status.toString() == 'ok'
            assert categoryId == category1.id
        }
    }

    def 'Should save an budget with no category and system category'(){
        given:'a budget command request body'
        BudgetCreateCommand cmd = generateCommand()
        def user1 = new User()
        def systemCategory1 = new SystemCategory()
        def budget = new Budget()
        budget.with {
            user = user1
            systemCategory = systemCategory1
            name = 'test name'
            warningPercentage = 0.7
            amount = 100.00
        }

        when:
        1 * budgetService.userService.getUser(_ as Long) >> user1
        1 * budgetService.systemCategoryService.find(_ as Long) >> systemCategory1
        0 * budgetService.categoryService.getById(_ as Long)
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget
        1 * budgetService.accountService.findAllByUser(_ as User) >> []

        def response = budgetService.create(cmd)

        then:
        assert response
        response.with {
            assert amount == 100.00
            assert spent == 0.00
            assert status.toString() == 'ok'
            assert categoryId == systemCategory1.id
        }
    }

    def 'Should edit an budget with no category and system category'(){
        given:'a budget command request body'
        BudgetUpdateCommand cmd = new BudgetUpdateCommand()
        cmd.with {
            name = "Food budget"
            amount= 1234.56
            categoryId = 123
        }

        def user1 = new User()
        def systemCategory1 = new SystemCategory()
        Budget budget = new Budget()
        budget.with {
            user = user1
            name = 'test name'
            warningPercentage = 0.7
            amount = 100.00
        }

        when:
        1 * budgetService.systemCategoryService.find(_ as Long) >> systemCategory1
        0 * budgetService.categoryService.getById(_ as Long)
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget
        1 * budgetService.accountService.findAllByUser(_ as User) >> []

        def response = budgetService.update(cmd, budget)

        then:
        assert response
        response.with {
            assert categoryId == systemCategory1.id
            assert name == cmd.name
            assert Math.round(amount * 100) / 100 == cmd.amount
        }
    }

    def 'Should edit an budget with category and no system category'(){
        given:'a budget command request body'
        BudgetUpdateCommand cmd = new BudgetUpdateCommand()
        cmd.with {
            name = "Food budget"
            amount= 1234.56
            categoryId = 123
        }

        def user1 = new User()
        def categoryToSet = new Category()
        categoryToSet.id = 666

        Budget budget = new Budget()
        budget.with {
            user = user1
            name = 'test name'
            warningPercentage = 0.7
            category = new Category()
        }

        when:
        1 * budgetService.systemCategoryService.find(_ as Long) >> null
        1 * budgetService.categoryService.getById(_ as Long) >> categoryToSet
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget
        1 * budgetService.transactionService.findAllByCategoryChargeAndDateFrom(_ as Category, _ as Date, _ as Boolean) >> []

        def response = budgetService.update(cmd, budget)

        then:
        assert response.with {
            assert categoryId == categoryToSet.id
            assert name == cmd.name
            assert Math.round(amount * 100) / 100 == cmd.amount
            assert spent == 0
            assert status.toString() == 'ok'
            assert leftToSpend == amount
        }
    }

    def "Should not get a budget and throw exception"(){

        when:
        1 * budgetService.budgetGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        budgetService.find(666)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'budget.notFound'
    }

    def "Should get all budget" () {
        Budget budget = new Budget()
        budget.with {
            user = new User()
            name = 'test name'
            warningPercentage = 0.7
            category = new Category()
        }

        when:
        1 * budgetService.budgetGormService.findAllByDateDeletedIsNull(_ as Map) >> [budget]
        1 * budgetService.transactionService.findAllByCategoryChargeAndDateFrom(_ as Category, _ as Date, _ as Boolean) >> []

        def response = budgetService.getAll()

        then:
        assert response instanceof  List<BudgetDto>
    }

    def "Should not get all budget" () {
        when:
        1 * budgetService.budgetGormService.findAllByDateDeletedIsNull(_ as Map) >> []
        def response = budgetService.getAll()

        then:
        response instanceof  List<BudgetDto>
        response.isEmpty()
    }

    def "Should get budgets by a cursor " () {
        given:
        Budget budget = new Budget()
        budget.with {
            user = new User()
            name = 'test name'
            warningPercentage = 0.7
            category = new Category()
        }

        def user = new User()
        def client = new Client()
        client.id = 1
        user.client = client

        when:
        1 * budgetService.userService.getUser(_ as Long) >> user
        1 * budgetService.securityService.getAuthentication() >> of(Principal)
        1 * budgetService.clientService.findByUsername(_ as String) >>  client
        1 * budgetService.budgetGormService.findAllByUserAndIdLessThanEqualsAndDateDeletedIsNull(_ as User,_ as Long, _ as Map) >> [budget]

        def response = budgetService.findAllByUserAndCursor(1L,2L)

        then:
        response instanceof  List<BudgetDto>
    }

    def "Should get a budget with analysis and get warning"(){

        given:
        SystemCategory systemCategory = new SystemCategory()
        systemCategory.id = 666

        User user = new User()
        user.with {
            name = 'test user'
        }

        Account account1 = new Account()
        account1.user = user


        Budget systemCategoryBudget = new Budget()
        systemCategoryBudget.with {
            systemCategoryBudget.user = user
            systemCategoryBudget.systemCategory = systemCategory
            name = 'system category budget'
            amount = 100.00
            warningPercentage = 0.7
        }

        Transaction transaction1 = new Transaction()
        transaction1.with {
            date = new Date()
            charge = true
            description = 'system category transaction'
            amount = 50
            transaction1.systemCategory = systemCategory
            account = account1
        }

        Transaction transaction2 = new Transaction()
        transaction2.with {
            date = new Date()
            charge = true
            description = 'system category transaction'
            amount = 20
            transaction1.systemCategory = systemCategory
            account = account1
        }

        when:
        1 * budgetService.budgetGormService.findByIdAndDateDeletedIsNull(_ as Long) >> systemCategoryBudget
        1 * budgetService.accountService.findAllByUser(_ as User) >> [account1]
        1 * budgetService.transactionService.findAllByAccountSystemCategoryChargeAndDateFrom(_ as Account, _ as SystemCategory, _ as Date, _ as Boolean) >> [transaction1, transaction2]

        BudgetDto result = budgetService.get(1L)

        then:
        result.with {
            assert status.toString() == 'warning'
            assert leftToSpend == 30
            assert spent == 70
            assert categoryId == systemCategory.id
        }
    }

    def "Should get a budget with analysis and get ok"(){

        given:
        SystemCategory systemCategory = new SystemCategory()
        systemCategory.id = 666

        User user = new User()
        user.with {
            name = 'test user'
        }

        Account account1 = new Account()
        account1.user = user


        Budget systemCategoryBudget = new Budget()
        systemCategoryBudget.with {
            systemCategoryBudget.user = user
            systemCategoryBudget.systemCategory = systemCategory
            name = 'system category budget'
            amount = 100.00
            warningPercentage = 0.7
        }

        Transaction transaction1 = new Transaction()
        transaction1.with {
            date = new Date()
            charge = true
            description = 'system category transaction'
            amount = 50
            transaction1.systemCategory = systemCategory
            account = account1
        }

        Transaction transaction2 = new Transaction()
        transaction2.with {
            date = new Date()
            charge = true
            description = 'system category transaction'
            amount = 10
            transaction1.systemCategory = systemCategory
            account = account1
        }

        when:
        1 * budgetService.budgetGormService.findByIdAndDateDeletedIsNull(_ as Long) >> systemCategoryBudget
        1 * budgetService.accountService.findAllByUser(_ as User) >> [account1]
        1 * budgetService.transactionService.findAllByAccountSystemCategoryChargeAndDateFrom(_ as Account, _ as SystemCategory, _ as Date, _ as Boolean) >> [transaction1, transaction2]

        BudgetDto result = budgetService.get(1L)

        then:
        result.with {
            assert status.toString() == 'ok'
            assert leftToSpend == 40
            assert spent == 60
            assert categoryId == systemCategory.id
        }
    }

    def "Should get a budget with analysis and get danger"(){

        given:
        SystemCategory systemCategory = new SystemCategory()
        systemCategory.id = 666

        User user = new User()
        user.with {
            name = 'test user'
        }

        Account account1 = new Account()
        account1.user = user


        Budget systemCategoryBudget = new Budget()
        systemCategoryBudget.with {
            systemCategoryBudget.user = user
            systemCategoryBudget.systemCategory = systemCategory
            name = 'system category budget'
            amount = 100.00
            warningPercentage = 0.7
        }

        Transaction transaction1 = new Transaction()
        transaction1.with {
            date = new Date()
            charge = true
            description = 'system category transaction'
            amount = 50
            transaction1.systemCategory = systemCategory
            account = account1
        }

        Transaction transaction2 = new Transaction()
        transaction2.with {
            date = new Date()
            charge = true
            description = 'system category transaction'
            amount = 150
            transaction1.systemCategory = systemCategory
            account = account1
        }

        when:
        1 * budgetService.budgetGormService.findByIdAndDateDeletedIsNull(_ as Long) >> systemCategoryBudget
        1 * budgetService.accountService.findAllByUser(_ as User) >> [account1]
        1 * budgetService.transactionService.findAllByAccountSystemCategoryChargeAndDateFrom(_ as Account, _ as SystemCategory, _ as Date, _ as Boolean) >> [transaction1, transaction2]

        BudgetDto result = budgetService.get(1L)

        then:
        result.with {
            assert status.toString() == 'danger'
            assert spent == 200
            assert leftToSpend == 0
            assert categoryId == systemCategory.id
        }
    }

    def "Should get a budget"(){

        given:
        SystemCategory systemCategory = new SystemCategory()
        systemCategory.id = 666

        User user = new User()
        user.with {
            name = 'test user'
        }

        Account account1 = new Account()
        account1.user = user

        Budget systemCategoryBudget = new Budget()
        systemCategoryBudget.with {
            systemCategoryBudget.user = user
            systemCategoryBudget.systemCategory = systemCategory
            name = 'system category budget'
            amount = 100.00
            warningPercentage = 0.7
        }

        when:
        1 * budgetService.budgetGormService.findByIdAndDateDeletedIsNull(_ as Long) >> systemCategoryBudget
        1 * budgetService.accountService.findAllByUser(_ as User) >> [account1]
        1 * budgetService.transactionService.findAllByAccountSystemCategoryChargeAndDateFrom(_ as Account, _ as SystemCategory, _ as Date, _ as Boolean) >> []

        BudgetDto result = budgetService.get(1L)

        then:
        result.with {
            assert status.toString() == 'ok'
            assert spent == 0
            assert leftToSpend == systemCategoryBudget.amount as BigDecimal
            assert categoryId == systemCategory.id
        }
    }

    private static BudgetCreateCommand generateCommand() {
        BudgetCreateCommand cmd = new BudgetCreateCommand()
        cmd.with {
            userId= 123
            categoryId = 123
            name = "Food budget"
            amount= 1234.56
        }
        cmd
    }

}
