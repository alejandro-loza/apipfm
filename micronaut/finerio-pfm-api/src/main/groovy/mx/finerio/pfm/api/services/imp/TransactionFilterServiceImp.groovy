package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.services.TransactionFilterService
import mx.finerio.pfm.api.validation.TransactionFiltersCommand

class TransactionFilterServiceImp implements TransactionFilterService {

    @Override
    List<TransactionDto> filterTransactions(List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd) {
        def filterMap =    ["categoryId" : categoryIdFilter,
                         "charge" : chargeFilter,
                         "minAmount": minAmountFilter,
                         "maxAmount": maxAmountFilter,
                         "dateFrom": fromDateFilter,
                         "dateTo":toDateFilter,
                         "description": descriptionFilter]
        List<List<TransactionDto>> filterLists = generateProperties(cmd).collect {
            filterMap[it.key as String](transactionDtos, cmd)
        }
        filterLists ? intersectResultList(filterLists): []
    }

    @Override
    Map<Object, Object> generateProperties(TransactionFiltersCommand cmd) {
        cmd.properties.findAll {
            if (it.getValue() != null && it.getKey() != 'class' && it.getKey() != 'cursor') {
                return it
            }
        }
    }

    private static List<TransactionDto> intersectResultList(List<List<TransactionDto>> filterLists) {
        List<TransactionDto> resultSet = filterLists?.first()
        filterLists.each {
            resultSet = resultSet.intersect(it)
        }
        resultSet
    }

    def categoryIdFilter = { List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.categoryId == cmd.categoryId }
    }

    def chargeFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.charge == cmd.charge}
    }

    def minAmountFilter = { List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.amount >= cmd.minAmount}
    }

    def maxAmountFilter = { List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.amount <= cmd.maxAmount}
    }

    def fromDateFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.date >= new Date(cmd.dateFrom)}
    }

    def toDateFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.date <=  new Date(cmd.dateTo)}
    }

    def descriptionFilter = {List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd ->
        transactionDtos.findAll {it.description ==  cmd.description}
    }

}
