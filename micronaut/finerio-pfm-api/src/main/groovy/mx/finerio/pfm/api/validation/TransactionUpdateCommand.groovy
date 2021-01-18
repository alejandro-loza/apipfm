package mx.finerio.pfm.api.validation

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

class TransactionUpdateCommand extends ValidationCommand{

    Long accountId
    Long date
    Boolean charge
    String description
    float  amount
    Long categoryId
}
