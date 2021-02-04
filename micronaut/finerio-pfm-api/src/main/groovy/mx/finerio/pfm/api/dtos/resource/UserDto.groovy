package mx.finerio.pfm.api.dtos.resource

import groovy.transform.ToString
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.ResourceDto

@ToString(includeNames = true, includePackage = false,
    includeSuperProperties = true)
class UserDto extends ResourceDto {

    String name

    UserDto() {}

    UserDto(User user) {
        this.id = user.id
        this.name = user.name
        this.dateCreated = user.dateCreated
    }
}
