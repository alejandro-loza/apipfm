package mx.finerio.pfm.api.services.security


import io.micronaut.security.authentication.providers.AuthoritiesFetcher
import io.reactivex.Flowable
import mx.finerio.pfm.api.services.gorm.ClientRoleGormService
import org.reactivestreams.Publisher

import javax.inject.Singleton

@Singleton
class AuthoritiesFetcherService implements AuthoritiesFetcher {

    protected final ClientRoleGormService clientRoleGormService

    AuthoritiesFetcherService(ClientRoleGormService userRoleGormService) {
        this.clientRoleGormService = userRoleGormService
    }

    @Override
    Publisher<List<String>> findAuthoritiesByUsername(String username) {
        Flowable.just(clientRoleGormService.findAllAuthoritiesByUsername(username))
    }

}