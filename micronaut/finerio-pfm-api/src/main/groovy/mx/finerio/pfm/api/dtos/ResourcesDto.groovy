package mx.finerio.pfm.api.dtos

class ResourcesDto {

    List<ResourceDto> data
    Long nextCursor

    ResourcesDto(List<UserDto> data) {
        this.data = data
        this.nextCursor = !data.isEmpty() ? calculateNextCursor(data?.last()?.id) : null
    }

    private Long calculateNextCursor(Long id) {
        id > 1 ? id - 1 : null
    }

}
