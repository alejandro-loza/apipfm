package mx.finerio.pfm.api.dtos

class ResourcesResponseDto {

    List<UserDto> users
    int nextCursor

    ResourcesResponseDto(List<UserDto> users) {
        this.users = users
        this.nextCursor = users?.last()?.id?.intValue()
    }

}
