package mx.finerio.pfm.api.services.security

import groovy.transform.CompileStatic
import io.micronaut.security.authentication.providers.UserFetcher
import io.micronaut.security.authentication.providers.UserState
import io.reactivex.Flowable
import mx.finerio.pfm.api.services.gorm.ClientGormService
import org.reactivestreams.Publisher

import javax.inject.Singleton

@CompileStatic
@Singleton
class ClientFetcherService implements UserFetcher {

    protected ClientGormService clientGormService

    UserFetcherService(ClientGormService userGormService) {
        this.clientGormService = userGormService
    }

    @Override
    Publisher<UserState> findByUsername(String username) {

        UserState user = clientGormService.findByUsername(username) as UserState
        (user ? Flowable.just(user) : Flowable.empty()) as Publisher<UserState>

    }

}