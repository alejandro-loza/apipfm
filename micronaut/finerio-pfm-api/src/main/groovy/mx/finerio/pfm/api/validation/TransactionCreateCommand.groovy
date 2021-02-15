package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@ToString(includeNames = true, includePackage = false)
class TransactionCreateCommand extends ValidationCommand{

    @NotNull(message= 'transaction.account.null')
    Long accountId
    @NotNull(message= 'transaction.date.null')
    Long date
    @NotNull(message= 'transaction.charge.null')
    boolean charge
    @NotNull(message= 'transaction.description.null')
    @NotBlank(message= 'transaction.description.blank')
    String description
    @NotNull(message= 'transaction.amount.null')
    @Positive
    @NotNull(message= 'transaction.amount.null')
    BigDecimal  amount

    Long categoryId

}
