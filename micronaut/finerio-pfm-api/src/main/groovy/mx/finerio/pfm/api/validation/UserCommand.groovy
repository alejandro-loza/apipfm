package mx.finerio.pfm.api.validation

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class UserCommand extends ValidationCommand {

    @NotNull(message= 'user.name.null')
    @NotBlank(message= 'user.name.blank')
    String name

}
