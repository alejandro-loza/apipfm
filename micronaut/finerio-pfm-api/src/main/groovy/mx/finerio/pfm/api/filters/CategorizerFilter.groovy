package mx.finerio.pfm.api.filters

import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import mx.finerio.pfm.api.config.CategorizerConfig
import org.reactivestreams.Publisher

class CategorizerFilter  implements HttpClientFilter {

    private final CategorizerConfig configuration

    CategorizerFilter(CategorizerConfig configuration) {
        this.configuration = configuration
    }

    @Override
    Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        chain.proceed(request.basicAuth(configuration.getUsername(), configuration.getPassword()))
    }
}
