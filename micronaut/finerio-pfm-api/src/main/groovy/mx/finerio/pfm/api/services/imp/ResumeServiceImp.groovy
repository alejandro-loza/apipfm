package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.utilities.BalancesDto
import mx.finerio.pfm.api.dtos.utilities.CategoryResumeDto
import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import mx.finerio.pfm.api.dtos.utilities.ResumeDto
import mx.finerio.pfm.api.dtos.utilities.SubCategoryResumeDto
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.dtos.utilities.TransactionsByDateDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.ResumeService
import  mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.validation.ResumeFilterParamsCommand

import javax.inject.Inject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.Collectors

class ResumeServiceImp implements ResumeService{

    public static final Date FROM_LIMIT = Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
    public static final boolean INCOME = false
    public static final boolean EXPENSE = true

    @Inject
    AccountService accountService

    @Inject
    TransactionService transactionsService


    @Override
    @Transactional
    List<MovementsDto> groupTransactionsByMonth(List<Transaction> transactionList){
        List<MovementsDto> movementsDtoList= []
        Map<String, List<Transaction>> list =  transactionList.stream()
                .collect( Collectors.groupingBy({ Transaction transaction ->
                    new SimpleDateFormat("yyyy-MM").format(transaction.date)
                }))
        for ( Map.Entry<String, List<Transaction>> entry : list.entrySet() ) {
            movementsDtoList.add( generateMovementDto( entry.key, entry.value ) )
        }
        movementsDtoList
    }

    @Override
    @Transactional
    ResumeDto getResume(Long userId, ResumeFilterParamsCommand cmd) {

        Date fromDate = cmd.dateFrom ? validateFromDate(cmd.dateFrom) : FROM_LIMIT
        Date toDate = cmd.dateTo ? validateToDate(cmd.dateTo , fromDate) : new Date()

        List<Account> accounts = cmd.accountId
                ? [accountService.getAccount(cmd.accountId)]
                : accountService.findAllByUserId(userId)

        List<MovementsDto> incomesResult = groupTransactionsByMonth(
                getAccountsTransactions(accounts, INCOME, fromDate, toDate))
        List<MovementsDto> expensesResult = groupTransactionsByMonth(
                getAccountsTransactions(accounts, EXPENSE, fromDate, toDate))

        ResumeDto resumeDto = new ResumeDto()
        resumeDto.incomes = incomesResult
        resumeDto.expenses = expensesResult
        resumeDto.balances =  getBalance(incomesResult, expensesResult)
        resumeDto
    }

    private static Date validateFromDate(Long dateFrom) {
        Date from = new Date(dateFrom)
        if(from.before(FROM_LIMIT)){
            throw new BadRequestException("date.range.invalid")
        }
        from
    }

    private static Date validateToDate(Long dateTo, Date from) {
        Date to =  new Date(dateTo)
        if(to.before(from)){
            throw new BadRequestException("date.range.invalid")
        }
        to
    }

    @Override
    List<BalancesDto> getBalance(List<MovementsDto> incomesResult, List<MovementsDto> expensesResult) {
        def lists = [incomesResult.collect {
            BalancesDto balance =new BalancesDto()
            balance.date = it.date
            balance.incomes = it.amount
            balance
        },
         expensesResult.collect {
             BalancesDto balance =new BalancesDto()
             balance.date = it.date
             balance.expenses = it.amount
             balance
         }]
        Map<Long, List<BalancesDto>>  resp = lists.flatten().stream()
                .collect ( Collectors.groupingBy({ BalancesDto balancesDto -> balancesDto.date}))
        resp.collect {  Long dateKey , List<BalancesDto> balances ->
           BalancesDto balancesDto = new BalancesDto()
            balancesDto.with {
                date = dateKey
                incomes = balances*.incomes.sum()
                expenses = balances*.expenses.sum()
            }
            balancesDto
        }
    }

    private List<CategoryResumeDto> getTransactionsGroupByParentCategory(List<Transaction> transactionList){
        List<CategoryResumeDto> categoryResumeDtos = []
        Map<Long, List<Transaction>> transactionsGrouped = transactionList.stream()
                .collect ( Collectors.groupingBy({ Transaction transaction ->
                    transaction.category.parent.id
                }))
        for ( Map.Entry<Long, List<Transaction>> entry : transactionsGrouped.entrySet() ) {
            categoryResumeDtos.add( generateParentCategoryResume( entry.key, entry.value ) )
        }
        categoryResumeDtos
    }

    List<SubCategoryResumeDto> getTransactionsGroupBySubCategory(List<Transaction> transactionList){
        List<SubCategoryResumeDto> subCategoryResumeDtos = []
        Map<Long, List<Transaction>> transactionsGrouped = transactionList.stream()
                .collect ( Collectors.groupingBy({ Transaction transaction ->
                    transaction.category.id
                }))
        for ( Map.Entry<Long, List<Transaction>> entry : transactionsGrouped.entrySet() ) {
            subCategoryResumeDtos.add( generateSubCategoryResume( entry.key, entry.value ) )
        }
        subCategoryResumeDtos
    }

    private List<TransactionsByDateDto> getTransactionsGroupByDay(List<Transaction> transactionList){

        Map<String, List<Transaction>> map = transactionList.groupBy { transaction ->
            new SimpleDateFormat("yyyy-MM-dd").format(transaction.date)
        }

        List<TransactionsByDateDto> list = []

        for ( Map.Entry<String, List<Transaction>> entry : map.entrySet() ) {
            list << generateTransactionByDate( entry.key, entry.value )
        }

        return list

    }

    private TransactionsByDateDto generateTransactionByDate(String stringDate, List<Transaction> transactionList){
        TransactionsByDateDto transactionsByDateDto = new TransactionsByDateDto()
        transactionsByDateDto.date = generateDate(stringDate).getTime()
        transactionsByDateDto.transactions = transactionList.collect{new TransactionDto(it)}
        transactionsByDateDto
    }

    private MovementsDto generateMovementDto(String stringDate, List<Transaction> transactions) {
        MovementsDto movementsDto = new MovementsDto()
        movementsDto.date = generateFixedDate(stringDate).getTime()
        movementsDto.categories = getTransactionsGroupByParentCategory(
                transactions )

        movementsDto.amount = transactions*.amount.sum() as float
        movementsDto
    }

    private CategoryResumeDto generateParentCategoryResume(Long parentId, List<Transaction> transactions) {
        CategoryResumeDto parentCategory = new CategoryResumeDto()
        parentCategory.categoryId = parentId
        parentCategory.subcategories = getTransactionsGroupBySubCategory(
                transactions)
        parentCategory.amount = transactions*.amount.sum() as float
        parentCategory
    }

    private SubCategoryResumeDto generateSubCategoryResume(Long parentId, List<Transaction> transactions) {
        SubCategoryResumeDto subCategoryResumeDto = new SubCategoryResumeDto()
        subCategoryResumeDto.categoryId = parentId
        subCategoryResumeDto.transactionsByDate =
                getTransactionsGroupByDay( transactions )
        subCategoryResumeDto.amount = transactions*.amount.sum() as float
        subCategoryResumeDto
    }

    private static Date generateFixedDate(String rawDate){
        generateDate("${rawDate}-01")
    }

    private static Date generateDate(String rawDate) {
        Date.from(LocalDate.parse(rawDate).atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private List<Transaction> getAccountsTransactions(List<Account> accounts, Boolean charge, Date dateFrom, Date dateTo) {
        List<Transaction> transactions = []
        for ( account in accounts ) {
            transactions.addAll(transactionsService
                    .findAllByAccountAndChargeAndDateRange(account, charge, dateFrom, dateTo))
        }
        transactions
    }

}
