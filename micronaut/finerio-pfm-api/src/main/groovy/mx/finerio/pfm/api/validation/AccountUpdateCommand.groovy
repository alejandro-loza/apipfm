package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ToString(includeNames = true, includePackage = false)
class AccountUpdateCommand extends ValidationCommand{

    Long userId
    Long financialEntityId

    @NotBlank(message= 'account.nature.blank')
    String nature

    @NotBlank(message= 'account.name.blank')
    String name

    @Size(min = 3, max =100, message= 'account.number.size')
    String number

    float balance

    boolean chargeable
}
