package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ToString(includeNames = true, includePackage = false)
class UserCommand extends ValidationCommand {

    @NotNull(message= 'user.name.null')
    @NotBlank(message= 'user.name.blank')
    String name

}
