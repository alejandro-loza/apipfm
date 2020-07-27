package mx.finerio.pfm.api.services


import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.services.imp.BudgetServiceImp
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import spock.lang.Specification

class BudgetServiceSpec extends Specification {

    BudgetService budgetService = new BudgetServiceImp()

    void setup(){
        budgetService.budgetGormService = Mock(BudgetGormService)
        budgetService.userService = Mock(UserService)
        budgetService.categoryService = Mock(CategoryService)
    }

    def 'Should save an budget'(){
        given:'a budget command request body'
        BudgetCreateCommand cmd = generateCommand()
        def user = new User()
        def category = new Category()

        when:
        1 * budgetService.userService.getUser(_ as Long) >> user
        1 * budgetService.categoryService.getById(_ as Long) >> category

        1 * budgetService.budgetGormService.save(_  as Budget) >> new Budget(cmd, user, category)

        def response = budgetService.create(cmd)

        then:
        response instanceof Budget
    }

    def "Should throw exception on null body"() {

        when:
        budgetService.create(null)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
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
        when:
        1 * budgetService.budgetGormService.findAllByUserAndIdLessThanEqualsAndDateDeletedIsNull(_ as Long, _ as Map) >> [budget]
        def response = budgetService.findAllByUserAndCursor(2)

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
