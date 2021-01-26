package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.TransactionFiltersCommand

interface TransactionFilterService {
  @Log
  List<TransactionDto> filterTransactions(List<TransactionDto> transactionDtos, TransactionFiltersCommand cmd)

  @Log
  Map<Object, Object> generateProperties(TransactionFiltersCommand cmd)
}