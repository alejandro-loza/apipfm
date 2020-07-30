package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ToString(includeNames = true, includePackage = false)
class FinancialEntityCreateCommand extends ValidationCommand {

    @NotNull(message= 'financialEntity.name.null')
    @NotBlank(message= 'financialEntity.name.blank')
    String name

    @NotNull(message= 'financialEntity.code.null')
    @NotBlank(message= 'financialEntity.code.blank')
    String code

}
