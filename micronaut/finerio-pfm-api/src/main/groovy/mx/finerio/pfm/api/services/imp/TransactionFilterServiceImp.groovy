package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.services.TransactionFilterService
import mx.finerio.pfm.api.validation.TransactionFiltersCommand

class TransactionFilterServiceImp implements TransactionFilterService {

    @Override
    List<TransactionDto> filterTransactions(List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd) {
        List<List<TransactionDto>> filterLists = generateProperties(cmd).collect {
            filterFunctionsMap()[it.key as String](transactionDtos, cmd)
        }
        filterLists ? intersectResultList(filterLists): []
    }

    private static List<TransactionDto> intersectResultList(List<List<TransactionDto>> filterLists) {
        List<TransactionDto> resultSet = filterLists?.first()
        filterLists.each {
            resultSet = resultSet.intersect(it)
        }
        resultSet
    }

    private Object filterFunctionsMap() {
        ["categoryId" : categoryIdFilter,
            "charge" : chargeFilter,
            "beginAmount": beginAmountFilter,
            "finalAmount": finalAmountFilter,
            "fromDate": fromDateFilter,
            "toDate":toDateFilter]
    }

    private static Map<Object, Object> generateProperties(TransactionFiltersCommand cmd) {
        cmd.properties.findAll {
            if (it.getValue() != null && it.getKey() != 'class' && it.getKey() != 'cursor') {
                return it
            }
        }
    }

    def categoryIdFilter = { List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.categoryId == cmd.categoryId }
    }

    def chargeFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.charge == cmd.charge}
    }

    def beginAmountFilter = {List<TransactionDto> transactionDtos,TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.amount >= cmd.beginAmount}
    }

    def finalAmountFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.amount <= cmd.finalAmount}
    }

    def fromDateFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.date >= new Date(cmd.fromDate)}
    }

    def toDateFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.date <=  new Date(cmd.toDate)}
    }

}
