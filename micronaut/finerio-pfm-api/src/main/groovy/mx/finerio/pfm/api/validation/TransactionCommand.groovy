package mx.finerio.pfm.api.validation

import groovy.transform.CompileStatic
import io.micronaut.core.annotation.Introspected

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

@Introspected
@CompileStatic
class TransactionCommand {

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
    float  amount
}
