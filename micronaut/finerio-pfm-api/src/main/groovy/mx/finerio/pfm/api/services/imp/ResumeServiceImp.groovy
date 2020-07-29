package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.BalancesDto
import mx.finerio.pfm.api.dtos.CategoryResumeDto
import mx.finerio.pfm.api.dtos.MovementsDto
import mx.finerio.pfm.api.dtos.ResumeDto
import mx.finerio.pfm.api.dtos.SubCategoryResumeDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.dtos.TransactionsByDateDto
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.ResumeService
import  mx.finerio.pfm.api.services.TransactionService
import mx.finerio.pfm.api.services.gorm.TransactionGormService

import javax.inject.Inject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

class ResumeServiceImp implements ResumeService{

    @Inject
    AccountService accountService

    @Inject
    TransactionService transactionsService

    @Override
    @Transactional
    List<Transaction> getExpenses(Long userId) {
        getAccountsTransactions(userId, true)
    }

    @Override
    @Transactional
    List<Transaction> getIncomes(Long userId) {
        getAccountsTransactions(userId, false)
    }

    @Override
    @Transactional
    List<MovementsDto> getTransactionsGroupByMonth(List<Transaction> transactionList){
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
    ResumeDto getResume(Long userId) {

        def incomes = getIncomes(userId)
        def expenses = getExpenses(userId)
        List<MovementsDto> incomesResult = getTransactionsGroupByMonth(incomes)
        List<MovementsDto> expensesResult = getTransactionsGroupByMonth( expenses)

        ResumeDto resumeDto = new ResumeDto()
        resumeDto.incomes = incomesResult
        resumeDto.expenses = expensesResult
        resumeDto.balances =  getBalance(incomesResult, expensesResult)
        resumeDto
    }

    @Override
    List<BalancesDto> getBalance(List<MovementsDto> incomesResult, List<MovementsDto> expensesResult) {
        [incomesResult.collect {
            [date: it.date, incomes: it.amount]
        },
         expensesResult.collect {
             [date: it.date, expenses: it.amount]
         }].transpose()*.sum() as List<BalancesDto>
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

    private List<Transaction> getAccountsTransactions(Long userId, Boolean charge) {
        List<Transaction> transactions = []
        def accounts = accountService.findAllByUserId( userId )

        for ( account in accounts ) {
            transactions.addAll(transactionsService.findAllByAccountAndCharge(account, charge))
        }
        transactions
    }

}
