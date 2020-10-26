package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ToString(includeNames = true, includePackage = false)
class AccountCreateCommand extends ValidationCommand{

    @NotNull(message= 'account.userID.null')
    Long userId

    @NotNull(message= 'account.financialEntityId.null')
    Long financialEntityId

    @NotNull(message= 'account.nature.null')
    @NotBlank(message= 'account.nature.blank')
    String nature

    @NotNull(message= 'account.name.null')
    @NotBlank(message= 'account.name.blank')
    String name

    @NotNull(message= 'account.number.null')
    @Size(min = 3, max =100, message= 'account.number.size')
    String number

    boolean chargeable

    BigDecimal balance = 0

}
