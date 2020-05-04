package mx.finerio.pfm.api.dtos

import mx.finerio.pfm.api.domain.User

class UserDto {

    Long id
    String name
    Date dateCreated

    UserDto() {}

    UserDto(User user) {
        this.id = user.id
        this.name = user.name
        this.dateCreated = user.dateCreated
    }
}
