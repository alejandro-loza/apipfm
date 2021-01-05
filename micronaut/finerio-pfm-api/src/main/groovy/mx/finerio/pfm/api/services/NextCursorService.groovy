package mx.finerio.pfm.api.services

import io.reactivex.Single
import mx.finerio.pfm.api.dtos.resource.ResourceDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto

interface NextCursorService {
    Single<ResourcesDto> generateResourcesDto(List<ResourceDto> dtos)
}
