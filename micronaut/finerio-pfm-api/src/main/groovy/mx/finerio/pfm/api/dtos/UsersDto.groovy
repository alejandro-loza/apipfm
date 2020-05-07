package mx.finerio.pfm.api.dtos

class UsersDto {

    List<UserDto> users
    int nextCursor

    UsersDto(List<UserDto> users) {
        this.users = users
        this.nextCursor = users?.last()?.id?.intValue()
    }

}
