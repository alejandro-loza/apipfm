package mx.finerio.pfm.api.services.imp

import io.micronaut.security.utils.SecurityService
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject

class ServiceTemplate {

    public static final int MAX_ROWS = 100

    @Inject
    ClientService clientService
    
    @Inject
    SecurityService securityService


    static void verifyBody(ValidationCommand cmd) {
        if (!cmd) {
            throw new IllegalArgumentException(
                    'request.body.invalid')
        }
    }

    Client getCurrentLoggedClient() {//todo verify if is all necesary in all services
        clientService.findByUsername(securityService.getAuthentication().get().name)
    }

}
