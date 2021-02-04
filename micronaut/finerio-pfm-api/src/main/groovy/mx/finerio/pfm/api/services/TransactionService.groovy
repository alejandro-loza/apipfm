package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.SystemCategory
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import mx.finerio.pfm.api.validation.TransactionUpdateCommand

interface TransactionService {

    @Log
    Transaction create(TransactionCreateCommand cmd)

    @Log
    Transaction find(Long id)

    @Log
    Transaction update(TransactionUpdateCommand cmd, Long id)

    @Log
    void delete(Long id)

    @Log
    List<TransactionDto> getAll()

    @Log
    List<TransactionDto> findAllByCursor(Long cursor)

    @Log
    List<TransactionDto> findAllByAccountAndCursor(Account account, Long cursor)

    @Log
    List<TransactionDto> findAllByAccount(Account account)

    @Log
    List<TransactionDto> findAllByCategory(Category category)

    @Log
    List<Transaction> findAllByCategoryChargeAndDateFrom(Category category, Date dateFrom, Boolean charge)

    @Log
    List<Transaction> findAllByAccountSystemCategoryChargeAndDateFrom(
            Account account, SystemCategory systemCategory, Date dateFrom, Boolean charge)

    @ Log
    List < Transaction > findAllByAccountAndChargeAndDateRange(Account account, Boolean charge, Date from, Date to)

    @Log
    List<Transaction> getAccountsTransactions(List<Account> accounts, Boolean charge, Date dateFrom, Date dateTo)

    @Log
    void deleteAllByAccount(Account account)

    @Log
    TransactionDto generateTransactionDto(transaction)
}
