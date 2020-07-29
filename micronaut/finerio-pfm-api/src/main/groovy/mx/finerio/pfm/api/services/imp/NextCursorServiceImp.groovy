package mx.finerio.pfm.api.services.imp

import io.reactivex.Single
import mx.finerio.pfm.api.dtos.resource.ResourceDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.services.NextCursorService

class NextCursorServiceImp extends ServiceTemplate implements NextCursorService {

    @Override
    Single<ResourcesDto> generateResourcesDto(List<ResourceDto> dtos) {
        if(dtos.size() == MAX_ROWS){
            Long nextCursor = dtos?.last()?.id
            dtos.removeLast()
            return Single.just(new ResourcesDto(dtos, nextCursor))
        }
        Single.just(new ResourcesDto(dtos, null))
    }
}
