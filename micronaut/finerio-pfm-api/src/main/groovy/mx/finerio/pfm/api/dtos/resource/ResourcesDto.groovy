package mx.finerio.pfm.api.dtos.resource

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.ToString

@JsonInclude(JsonInclude.Include.ALWAYS)
@ToString(includeNames = true, includePackage = false)
class ResourcesDto {

    List data
    Long nextCursor

    ResourcesDto(List data, Long nextCursor) {
        this.data = data
        this.nextCursor = nextCursor
    }

}
