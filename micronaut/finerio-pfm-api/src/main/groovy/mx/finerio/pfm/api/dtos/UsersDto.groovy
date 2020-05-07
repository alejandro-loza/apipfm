package mx.finerio.pfm.api.dtos

class UsersDto {

    List<UserDto> data
    Long nextCursor

    UsersDto(List<UserDto> data) {
        this.data = data
        this.nextCursor = data?.last()?.id != 1? data?.last()?.id -1 : null
    }

}
