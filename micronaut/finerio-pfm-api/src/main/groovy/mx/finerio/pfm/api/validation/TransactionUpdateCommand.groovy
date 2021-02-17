package mx.finerio.pfm.api.validation

class TransactionUpdateCommand extends ValidationCommand{

    Long accountId
    Long date
    Boolean charge
    String description
    float  amount
    Long categoryId
}
