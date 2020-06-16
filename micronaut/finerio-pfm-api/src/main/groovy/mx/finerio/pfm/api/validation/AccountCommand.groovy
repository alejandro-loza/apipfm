package mx.finerio.pfm.api.validation

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

class AccountCommand extends ValidationCommand{

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
    @Size(min = 13, max =100, message= 'account.number.size')
    String number

    float balance
}
