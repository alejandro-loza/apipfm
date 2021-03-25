package mx.finerio.pfm.api.validation

import groovy.transform.ToString
import mx.finerio.pfm.api.enums.WebhookNatureEnum

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ToString(includeNames = true, includePackage = false)
class WebHookUpdateCommand extends ValidationCommand  {
    String url
}
