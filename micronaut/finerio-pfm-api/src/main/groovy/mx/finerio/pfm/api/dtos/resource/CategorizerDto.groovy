package mx.finerio.pfm.api.dtos.resource

import io.micronaut.core.annotation.Introspected

@Introspected
class CategorizerDto {
    Long id
    String description
    Long categoryId
    CategorizerDto parent
}
