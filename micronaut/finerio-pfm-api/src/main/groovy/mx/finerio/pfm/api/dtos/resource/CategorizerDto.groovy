package mx.finerio.pfm.api.dtos.resource

import io.micronaut.core.annotation.Introspected

@Introspected
class CategorizerDto {
    String id
    String description
    String categoryId
    CategorizerDto parent
}
