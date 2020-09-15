package mx.finerio.pfm.api.clients

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import io.reactivex.Flowable
import io.reactivex.Maybe
import mx.finerio.pfm.api.config.CategorizerConfig

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LowLevelClient {

    @Inject
    @Client(CategorizerConfig.CATEGORIZER_API_URL)
    RxHttpClient httpClient

    Maybe<List<Map>> fetchPackages(String input) {
        def url  = UriBuilder.of("/search")
                .queryParam('input',input)
                .build()
        HttpRequest<?> req = HttpRequest.GET(url)
        Flowable flowable = httpClient.retrieve(req, Argument.listOf(Map.class))
        return (Maybe<List<Map>>) flowable.firstElement()
    }

}