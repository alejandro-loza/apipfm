package mx.finerio.pfm.api.services.imp

import io.micronaut.security.utils.SecurityService
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject

class ServiceTemplate {

    public static final int MAX_ROWS = 101

    @Inject
    ClientService clientService
    
    @Inject
    SecurityService securityService


    int getMaxRows(){
        MAX_ROWS
    }

    static void verifyBody(ValidationCommand cmd) {
        if (!cmd) {
            throw new IllegalArgumentException(
                    'request.body.invalid')
        }
    }

    Client getCurrentLoggedClient() {
        clientService.findByUsername(securityService.getAuthentication().get().name)
    }

}
