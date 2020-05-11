package mx.finerio.pfm.api.dtos

class UsersDto {

    List<UserDto> data
    Long nextCursor

    UsersDto(List<UserDto> data) {
        this.data = data
        this.nextCursor = !data.isEmpty() ? calculateNextCursor(data?.last()?.id) : null
    }

    private Long calculateNextCursor(Long id) {
        id > 1 ? id - 1 : null
    }

}
