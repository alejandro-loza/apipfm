package mx.finerio.pfm.api.dtos

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.ALWAYS)
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
