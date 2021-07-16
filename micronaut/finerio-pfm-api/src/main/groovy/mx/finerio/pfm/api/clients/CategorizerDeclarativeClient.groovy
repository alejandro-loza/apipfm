package mx.finerio.pfm.api.clients

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import mx.finerio.pfm.api.dtos.resource.CategorizerDto
import mx.finerio.pfm.api.dtos.resource.CleanerDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto

@Client(value = '${categorizer.url}', errorType = ErrorsDto)
interface CategorizerDeclarativeClient{

    @Get("/search")
    CategorizerDto getCategories(@Header String authorization,
        @QueryValue('input') String input,
        @QueryValue('income') Boolean income,
        @QueryValue('clean') Boolean clean)

    @Get("/clean")
    CleanerDto cleanText(@Header String authorization,
        @QueryValue('input') String input,
        @QueryValue('income') Boolean income )

}
