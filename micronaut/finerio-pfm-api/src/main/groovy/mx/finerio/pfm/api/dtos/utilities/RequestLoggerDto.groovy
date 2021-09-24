package mx.finerio.pfm.api.dtos.utilities

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString
import mx.finerio.pfm.api.dtos.resource.ResourceDto
import mx.finerio.pfm.api.enums.EventType

@JsonInclude(JsonInclude.Include.ALWAYS)
@ToString(includeNames = true, includePackage = false,
        includeSuperProperties = true)
class RequestLoggerDto  extends ResourceDto{
    Long userId
    EventType eventType
}
