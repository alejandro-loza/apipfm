package mx.finerio.pfm.api.clients

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.reactivex.Flowable
import mx.finerio.pfm.api.config.CategorizerConfig
import mx.finerio.pfm.api.dtos.resource.CategorizerDto

@Client(CategorizerConfig.CATEGORIZER_API_URL)
interface CategorizerClient {
    @Get("{/search}")
    Flowable<CategorizerDto> fetchData(@QueryValue('input') String input)
}