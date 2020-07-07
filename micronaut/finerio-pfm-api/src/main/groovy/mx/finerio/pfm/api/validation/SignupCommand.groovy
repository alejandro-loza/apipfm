package mx.finerio.pfm.api.validation

import groovy.transform.ToString
import io.micronaut.core.annotation.Introspected

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@ToString(includeNames = true, includePackage = false,
        excludes = 'password')
@Introspected
class SignupCommand {

    @NotBlank(message = "signup.name.blank")
    @Size(min = 1, max = 50, message = 'signup.name.size.invalid')
    String name

    @NotBlank(message = "signup.firstLastName.blank")
    @Size(min = 1, max = 20, message = 'signup.firstLastName.size.invalid')
    String firstLastName

    @NotBlank(message = "signup.secondLastName.blank")
    @Size(min = 1, max = 20, message = 'signup.secondLastName.size.invalid')
    String secondLastName

    @NotBlank(message = "signup.email.blank")
    @Size(min = 1, max = 100, message = 'signup.email.size.invalid')
    String email

    @NotBlank(message = "signup.companyName.blank")
    @Size(min = 1, max = 20, message = 'signup.companyName.size.invalid')
    String companyName

    @NotBlank(message = "signup.username.blank")
    @Size(min = 1, max = 30, message = 'signup.username.size.invalid')
    String username

    @NotBlank(message = "signup.password.blank")
    @Size(min = 1, max = 100, message = 'signup.password.size.invalid')
    String password

}
