package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.CategoryResumeDto
import mx.finerio.pfm.api.dtos.MovementsDto
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.services.AccountService
import mx.finerio.pfm.api.services.ResumeService
import  mx.finerio.pfm.api.services.TransactionService

import javax.inject.Inject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId

class ResumeServiceImp implements ResumeService{

    @Inject
    AccountService accountService

    @Inject
    TransactionService transactionsService

    @Override
    List<Transaction> getExpenses(Long userId) {
        getAccountsTransactions(userId, true)
    }

    @Override
    List<Transaction> getIncomes(Long userId) {
        getAccountsTransactions(userId, false)
    }

    @Override
    List<MovementsDto> getTransactionsGroupByMonth(List<Transaction> transactionList){
        transactionList.groupBy { transaction ->
            new SimpleDateFormat("yyyy-MM").format(transaction.date)
        }.collect{ stringDate , transactions ->
            generateMovementDto(stringDate, transactions)
        }
    }

    @Override
    List<CategoryResumeDto> getTransactionsGroupByParentCategory(List<Transaction> transactionList){
        transactionList.groupBy { transaction ->
            transaction.category.parent.id
         }.collect{ parentId , transactions ->
            generateParentCategoryResume(parentId, transactions)
        }
    }

    private MovementsDto generateMovementDto(String stringDate, List<Transaction> transactions) {
        MovementsDto movementsDto = new MovementsDto()
        movementsDto.with {
            date = generateDate(stringDate).getTime()
            categories = getTransactionsGroupByParentCategory( transactions)
            amount = transactions*.amount.sum() as float
        }
        movementsDto
    }

    private static CategoryResumeDto generateParentCategoryResume(Long parentId, List<Transaction> transactions) {
        CategoryResumeDto parentCategory = new CategoryResumeDto()
        parentCategory.with {
            categoryId = parentId
            subcategories = transactions
            amount = transactions*.amount.sum() as float
        }
        parentCategory
    }

    private static Date generateDate(String rawDate){
         Date.from(LocalDate.parse("${rawDate}-01").atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    private List<Transaction> getAccountsTransactions(Long userId, Boolean charge) {
        List<Transaction> transactions = []
        accountService.findAllByUserId(userId).each { account ->
            transactions.addAll(transactionsService.findAllByAccountAndCharge(account, charge))
        }
        transactions
    }

}
