package mx.finerio.pfm.api.services

import io.micronaut.security.utils.SecurityService
import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.services.imp.BudgetServiceImp
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand
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
    }

    def 'Should save an budget with category and no system category'(){
        given:'a budget command request body'
        BudgetCreateCommand cmd = generateCommand()
        def user1 = new User()
        def category1 = new Category()
        def budget = new Budget()
        budget.with {
            user = user1
            category = category1
            name = 'test name'
        }

        when:
        1 * budgetService.userService.getUser(_ as Long) >> user1
        1 * budgetService.systemCategoryService.find(_ as Long) >> null
        1 * budgetService.categoryService.getById(_ as Long) >> category1
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget

        def response = budgetService.create(cmd)

        then:
        response instanceof Budget
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
        }

        when:
        1 * budgetService.userService.getUser(_ as Long) >> user1
        1 * budgetService.systemCategoryService.find(_ as Long) >> systemCategory1
        0 * budgetService.categoryService.getById(_ as Long)
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget

        def response = budgetService.create(cmd)

        then:
        response instanceof Budget
    }

    def 'Should edit an budget with no category and system category'(){
        given:'a budget command request body'
        BudgetUpdateCommand cmd = new BudgetUpdateCommand()
        cmd.with {
            userId= 123
            name = "Food budget"
            amount= 1234.56
            categoryId = 123
        }

        def user1 = new User()
        def systemCategory1 = new SystemCategory()
        Budget budget = new Budget()
        budget.with {
            user = user1
            systemCategory = systemCategory1
            name = 'test name'
        }

        when:
        1 * budgetService.systemCategoryService.find(_ as Long) >> systemCategory1
        0 * budgetService.categoryService.getById(_ as Long)
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget

        def response = budgetService.update(cmd, budget)

        then:
        response instanceof Budget
    }

    def 'Should edit an budget with category and no system category'(){
        given:'a budget command request body'
        BudgetUpdateCommand cmd = new BudgetUpdateCommand()
        cmd.with {
            userId= 123
            name = "Food budget"
            amount= 1234.56
            categoryId = 123
        }

        def user1 = new User()
        def categoryToSet = new Category()
        Budget budget = new Budget()
        budget.with {
            user = user1
            category = categoryToSet
            name = 'test name'
        }

        when:
        1 * budgetService.systemCategoryService.find(_ as Long) >> null
        1 * budgetService.categoryService.getById(_ as Long) >> categoryToSet
        1 * budgetService.budgetGormService.save(_  as Budget) >> budget

        def response = budgetService.update(cmd, budget)

        then:
        response instanceof Budget
    }

    def "Should get a budget"(){

        when:
        1 * budgetService.budgetGormService.findByIdAndDateDeletedIsNull(_ as Long) >> new Budget()

        def result = budgetService.find(1L)

        then:
        result instanceof Budget
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
        budget.user = new User()
        budget.category = new Category()
        when:
        1 * budgetService.budgetGormService.findAllByDateDeletedIsNull(_ as Map) >> [budget]
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
        budget.user = new User()
        budget.category = new Category()

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
