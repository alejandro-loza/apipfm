package mx.finerio.pfm.api.dtos

class UsersDto {

    List<UserDto> data
    int nextCursor

    UsersDto(List<UserDto> data) {
        this.data = data
        this.nextCursor = data?.last()?.id?.intValue()
    }

}
