package mx.finerio.pfm.api.dtos

class SubCategoryResumeDto {
    Long categoryId
    float amount
    List<TransactionsByDateDto> transactionsByDate
}
