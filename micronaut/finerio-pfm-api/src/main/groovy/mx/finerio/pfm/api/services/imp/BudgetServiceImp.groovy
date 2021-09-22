package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.*
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.*
import mx.finerio.pfm.api.services.gorm.BudgetGormService
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import mx.finerio.pfm.api.validation.BudgetUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject
import java.time.ZonedDateTime

class BudgetServiceImp extends ServiceTemplate implements BudgetService {

    public static final BigDecimal DEFAULT_WARNING_PERCENTAGE = 0.7
    public static final boolean EXPENSE = true

    @Inject
    BudgetGormService budgetGormService

    @Inject
    UserService userService

    @Inject
    CategoryService categoryService

    @Inject
    SystemCategoryService systemCategoryService

    @Inject
    TransactionService transactionService

    @Inject
    AccountService accountService

    @Override
    @Transactional
    Budget create(BudgetCreateCommand cmd){
        Budget budget = new Budget()
        User userToSet = userService.findUser(cmd.userId)
        budget.with {
            user = userToSet
            name = cmd.name
            amount = cmd.amount
            warningPercentage = cmd.warningPercentage ?: DEFAULT_WARNING_PERCENTAGE
        }
        setCategoryOrSystemCategory(cmd, budget, userToSet)
        budgetGormService.save(budget)
    }

    @Override
    Budget find(Long id) {
        Optional.ofNullable(budgetGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('budget.notFound') })
    }

    @Override
    BudgetDto get(Long id) {
        crateBudgetDtoWithAnalysis(find(id))
    }

    @Override
    BudgetDto update(BudgetUpdateCommand cmd, Budget budget){

        User userToSet = cmd.userId ? userService.getUser(cmd.userId) : budget.user
        budget.with {
            user = userToSet
            name = cmd.name ?: budget.name
            amount = cmd.amount ?: budget.amount
            warningPercentage = cmd.warningPercentage ?: budget.warningPercentage
        }
        setCategoryOrSystemCategory(cmd, budget, userToSet)
        crateBudgetDtoWithAnalysis(budgetGormService.save(budget))
    }

    @Override
    void delete(Long id){
        Budget budget = find(id)
        budget.dateDeleted = new Date()
        budgetGormService.save(budget)
    }

    @Override
    List<BudgetDto> getAll() {
        budgetGormService
                .findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{crateBudgetDtoWithAnalysis(it)}
    }

    @Override
    List<BudgetDto> findAllByUserAndCursor(Long userId, Long cursor) {
        User user = userService.findUser(userId)
        verifyLoggedClient(user.client)
        List<Budget> budgets = budgetGormService
                .findAllByUserAndIdLessThanEqualsAndDateDeletedIsNull(
                        user, cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])

        generateBudgetsDtos(user, budgets)
    }

    @Override
    List<BudgetDto> findAllByUserId(Long userId) {
        findAllByUser(userService.getUser(userId))
    }

    @Override
    List<BudgetDto>findAllByUser(User user) {
        verifyLoggedClient(user.client)
        List<Budget> budgets = budgetGormService
           .findAllByUserAndDateDeletedIsNull(user, [ sort: 'id', order: 'desc'])

        generateBudgetsDtos(user, budgets)
    }

    @Override
    Budget findByUserAndCategory(User user, Category category){
        budgetGormService.findByUserAndCategoryAndDateDeletedIsNull(user, category)
    }

    @Override
    Budget findByUserAndSystemCategory(User user, SystemCategory category){
        budgetGormService.findByUserAndSystemCategoryAndDateDeletedIsNull(user, category)
    }

    @Override
    Budget findByCategory(Category category) {
        budgetGormService.findByCategoryAndDateDeletedIsNull(category)
    }

    @Override
    BudgetDto crateBudgetDtoWithAnalysis(Budget budget) {
        List<Transaction> thisMonthTransactions = budget.systemCategory ?
                getThisMonthUserAccountsSystemCategoryExpenses(budget)
                : transactionService
                .findAllByCategoryChargeAndDateFrom(budget.category, getFirstDayOfCurrentMonth(), EXPENSE)

        return generateBudgetDto(budget, thisMonthTransactions)
    }

    private List<BudgetDto> generateBudgetsDtos(User user, List<Budget> budgets) {
        List<Transaction> thisMonthTransactions = getThisMonthUserAccountsExpenses(user)

        def systemCategoryTransactions = thisMonthSystemTransactions(thisMonthTransactions)
        def categoryTransactions = thisMonthCategoryTransactions(thisMonthTransactions)

        List<BudgetDto> systemCategoryBudgets = budgets.findAll { it.systemCategory }.collect { Budget budget ->
            generateBudgetDto(budget, systemCategoryTransactions[budget.systemCategory.id])
        }

        List<BudgetDto> categoryBudgets = budgets.findAll { it.category }.collect { Budget budget ->
            generateBudgetDto(budget, categoryTransactions[budget.category.id])
        }
        systemCategoryBudgets + categoryBudgets
    }

    private void verifyLoggedClient(Client client) {
        if (client.id != getCurrentLoggedClient().id) {
            throw new ItemNotFoundException('account.notFound')
        }
    }

    private Category findCategoryToSet(Long categoryId, User user) {
        Category categoryToSet = categoryService.getById(categoryId)
        if (categoryToSet
                && this.findByUserAndCategory(user, categoryToSet)) {
            throw new BadRequestException('budget.category.nonUnique')
        }
        categoryToSet
    }

    private static BudgetDto.StatusEnum calculateStatus(Budget budget, float spend) {
        def limit = budget.amount * budget.warningPercentage
        if(spend < limit ){
            return BudgetDto.StatusEnum.ok
        }
        if(spend >= limit && spend < budget.amount){
            return BudgetDto.StatusEnum.warning
        }
        else{
            return BudgetDto.StatusEnum.danger
        }
    }

    private List<Transaction> getThisMonthUserAccountsExpenses(User user) {
        List<Transaction> thisMonthTransactions = []
        accountService.findAllByUser(user).each { Account account ->
            thisMonthTransactions.addAll(transactionService.findAllByAccountAndChargeAndDateRange(
                    account, EXPENSE, getFirstDayOfCurrentMonth(), new Date()))
        }
        thisMonthTransactions
    }

    private List<Transaction> getThisMonthUserAccountsSystemCategoryExpenses(Budget budget) {
        List<Transaction> thisMonthTransactions = []
        accountService.findAllByUser(budget.user).each { Account account ->
            thisMonthTransactions.addAll(transactionService.findAllByAccountSystemCategoryChargeAndDateFrom(
                    account, budget.systemCategory, getFirstDayOfCurrentMonth(), EXPENSE))
        }
        thisMonthTransactions
    }

    private static Map<Long, List<Transaction>> thisMonthCategoryTransactions(List<Transaction> thisMonthTransactions) {
        thisMonthTransactions.findAll { it.category }
                .groupBy { it.category.id }
    }

    private static Map<Long, List<Transaction>> thisMonthSystemTransactions(List<Transaction> thisMonthTransactions) {
        thisMonthTransactions.findAll { it.systemCategory }
                .groupBy { it.systemCategory.id }
    }

    private static BudgetDto generateBudgetDto(Budget budget, transactions) {
        BudgetDto budgetDto = new BudgetDto()
        budgetDto.with {
            id = budget.id
            categoryId = budget.systemCategory ? budget.systemCategory.id : budget.category.id
            name = budget.name
            amount = budget.amount
            warningPercentage = budget.warningPercentage
            spent = transactions ? transactions*.amount.sum() as float : 0
            leftToSpend =  amount - spent > 0 ? amount - spent : 0
            status = calculateStatus(budget, spent)
            dateCreated = budget.dateCreated
            lastUpdated = budget.lastUpdated
        }
        budgetDto
    }

    void setCategoryOrSystemCategory(ValidationCommand cmd, Budget budget, User userToSet) {
        Long categoryId = cmd["categoryId"] as Long
        if (!categoryId)  return

        SystemCategory systemCategory = systemCategoryService.find(categoryId)
        if (systemCategory && this.findByUserAndSystemCategory(userToSet, systemCategory)) {
            throw new BadRequestException('budget.category.nonUnique')
        }
        if (systemCategory){
            budget.systemCategory = systemCategory
        }
        else {
            budget.category = findCategoryToSet(categoryId, userToSet)
        }
    }

}
