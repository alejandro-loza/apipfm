package mx.finerio.pfm.api.services.imp

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import io.micronaut.http.client.exceptions.HttpClientResponseException
import mx.finerio.pfm.api.clients.CategorizerDeclarativeClient
import mx.finerio.pfm.api.dtos.resource.CategorizerDto
import mx.finerio.pfm.api.services.CategorizerService

import javax.inject.Inject

@ConfigurationProperties('categorizer')
@Requires(property = 'categorizer')
class CategorizerServiceImp implements CategorizerService {

    private String username

    private String password

    @Inject
    CategorizerDeclarativeClient categorizerDeclarativeClient

    CategorizerDto searchCategory(String description){
        try {
            return  categorizerDeclarativeClient.getCategories(getAuthorizationHeader(), description)
        } catch (HttpClientResponseException e) {
            return new CategorizerDto()
        }
    }

    private String getAuthorizationHeader()  throws Exception {
        def authEncoded = "${username}:${password}"
                .bytes.encodeBase64().toString()
        "Basic ${authEncoded}"
    }
}
