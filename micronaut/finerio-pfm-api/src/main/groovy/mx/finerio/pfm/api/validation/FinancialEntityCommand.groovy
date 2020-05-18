package mx.finerio.pfm.api.validation

import groovy.transform.CompileStatic
import io.micronaut.core.annotation.Introspected

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
@CompileStatic
class FinancialEntityCommand {

    @NotNull(message= 'financialEntity.name.null')
    @NotBlank(message= 'financialEntity.name.blank')
    String name

    @NotNull(message= 'financialEntity.code.null')
    @NotBlank(message= 'financialEntity.code.blank')
    String code

}
