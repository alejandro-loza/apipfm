package mx.finerio.pfm.api.dtos

import mx.finerio.pfm.api.domain.User

class UserDto {
    String username
    String firstName
    String lastName
    String email
    Long phone
    int userStatus

    UserDto(User user) {
        this.username = user.username
        this.firstName = user.firstName
        this.lastName = user.lastName
        this.email = user.email
        this.phone = user.phone
        this.userStatus = user.userStatus
    }
}
