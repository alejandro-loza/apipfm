package mx.finerio.pfm.api.clients

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import mx.finerio.pfm.api.config.CategorizerConfig
import mx.finerio.pfm.api.dtos.resource.CategorizerDto
import org.springframework.beans.factory.annotation.Value

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ConfigurationProperties(CategorizerConfig.PREFIX)
@Requires(property = CategorizerConfig.PREFIX)
class CategorizerClient {

    @Inject
    @Client(CategorizerConfig.CATEGORIZER_API_URL)
    RxHttpClient httpClient

    @Value('${cagorizer.username}')
    String username

    @Value('${cagorizer.password}')
    String password

    CategorizerDto fetchCategory(String input) {
        def url  = UriBuilder.of("/search")
                .queryParam('input',input)
                .build()
        httpClient.toBlocking().exchange(HttpRequest.GET(url)
                .basicAuth(username, password
                ), CategorizerDto).body()
    }

}