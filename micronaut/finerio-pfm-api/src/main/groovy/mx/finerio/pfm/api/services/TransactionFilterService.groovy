package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.validation.TransactionFiltersCommand

interface TransactionFilterService {
  List<TransactionDto> filterTransactions(List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd)
}