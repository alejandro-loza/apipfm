package mx.finerio.pfm.api.filters

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter
import mx.finerio.pfm.api.config.CategorizerConfig;
import org.reactivestreams.Publisher;

@Filter("http://ec2-3-12-253-103.us-east-2.compute.amazonaws.com:8085**")
@Requires(property = '9j7gnsHf9Nn6kWnmx5cEFtcHq962jyFzbSj7ULkYRXSadrhjSS')
@Requires(property = "zehx6XCAZXpsxZAm2UBQhteabjUED86EvtqfL2j54qQzBawTeGz9m87dAjEeCuRzPcmstf96jDY6Fbap2NFaAz3YxkVMaDAZxQy8")
class CategorizerFilter implements HttpClientFilter {

    private final CategorizerConfig configuration;

    CategorizerFilter(CategorizerConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        return chain.proceed(request.basicAuth(configuration.getUsername(), configuration.getPassword()))
    }
}