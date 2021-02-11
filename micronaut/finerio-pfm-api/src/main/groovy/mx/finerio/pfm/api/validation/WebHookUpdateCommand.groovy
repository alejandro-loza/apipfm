package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ToString(includeNames = true, includePackage = false)
class WebHookUpdateCommand extends ValidationCommand  {
    String url
    String nature
}
