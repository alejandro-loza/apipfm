package mx.finerio.pfm.api.pogos


import groovy.transform.CompileStatic
import groovy.transform.ToString
import io.micronaut.core.annotation.Introspected

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
@CompileStatic
class UserCreateCommand {

    @NotNull(message= "User name is null")
    @NotBlank(message= "User name is blank")
    String name

}
