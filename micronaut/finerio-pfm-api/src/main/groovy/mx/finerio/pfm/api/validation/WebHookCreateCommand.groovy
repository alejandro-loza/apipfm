package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ToString(includeNames = true, includePackage = false)
class WebHookCreateCommand extends ValidationCommand  {

    @NotNull(message= 'webhook.url.null')
    @NotBlank(message= 'webhook.url.blank')
    String url

    @NotNull(message= 'webhook.nature.null')
    @NotBlank(message= 'webhook.nature.blank')
    String nature
}
