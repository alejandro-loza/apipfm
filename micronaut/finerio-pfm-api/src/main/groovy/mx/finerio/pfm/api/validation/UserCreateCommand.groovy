package mx.finerio.pfm.api.validation


import groovy.transform.CompileStatic
import io.micronaut.core.annotation.Introspected

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
@CompileStatic
class UserCreateCommand {

    @NotNull(message= "User name is null")
    @NotBlank(message= "User name is blank")
    String name

}
