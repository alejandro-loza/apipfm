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

    @NotNull
    @NotBlank
    String username

    @NotNull
    @NotBlank
    String firstName

    @NotNull
    @NotBlank
    String lastName

    @NotNull
    @NotBlank
    @Email
    String email

    @NotNull
    @NotBlank
    Long phone

    @NotNull
    int userStatus
}
