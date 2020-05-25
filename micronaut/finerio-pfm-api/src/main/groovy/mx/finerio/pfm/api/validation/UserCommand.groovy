package mx.finerio.pfm.api.validation


import groovy.transform.CompileStatic
import io.micronaut.core.annotation.Introspected

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
@CompileStatic
class UserCommand {

    @NotNull(message= 'user.name.null')
    @NotBlank(message= 'user.name.blank')
    String name

}
