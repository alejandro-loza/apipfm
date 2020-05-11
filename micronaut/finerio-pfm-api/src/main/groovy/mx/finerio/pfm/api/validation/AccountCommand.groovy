package mx.finerio.pfm.api.validation

import groovy.transform.CompileStatic
import io.micronaut.core.annotation.Introspected

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@CompileStatic
class AccountCommand {

    @NotNull(message= 'account.userID.null')
    @NotBlank(message= 'account.userID.blank')
    Long userId

    @NotNull(message= 'account.financialEntityId.null')
    @NotBlank(message= 'account.financialEntityId.blank')
    Long financialEntityId

    @NotNull(message= 'account.nature.null')
    @NotBlank(message= 'account.nature.blank')
    String nature

    @NotNull(message= 'account.name.null')
    @NotBlank(message= 'account.name.blank')
    String name

    @NotNull(message= 'account.number.null')
    @NotBlank(message= 'account.number.blank')
    @Size(min = 13, max =16, message= 'account.number.size')
    Long number

    float balance
}
