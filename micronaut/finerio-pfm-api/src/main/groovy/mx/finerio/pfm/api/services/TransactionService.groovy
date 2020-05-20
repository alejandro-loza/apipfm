package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.TransactionDto
import mx.finerio.pfm.api.validation.TransactionCommand

interface TransactionService {
    Transaction create(TransactionCommand cmd)
    Transaction find(Long id)
    Transaction update(TransactionCommand cmd, Long id)
    void delete(Long id)
    List<TransactionDto> getAll()
    List<TransactionDto> findAllByCursor(Long cursor)
}