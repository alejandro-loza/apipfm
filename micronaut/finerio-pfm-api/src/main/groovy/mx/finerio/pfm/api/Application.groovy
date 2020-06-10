package mx.finerio.pfm.api

import groovy.transform.CompileStatic
import io.micronaut.runtime.Micronaut
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.validation.ClientCommand

import javax.inject.Singleton
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent

@CompileStatic
@Singleton
class Application implements ApplicationEventListener<ServerStartupEvent> {

    protected final ClientService registerService

    Application(ClientService registerService) {
        this.registerService = registerService
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        ClientCommand cmd = new ClientCommand()
        cmd.with {
            username = 'sherlock'
            rawPassword = 'elementary'
            authorities = ['ROLE_DETECTIVE']
        }
        registerService.register(cmd)
    }

    static void main(String[] args) {
        Micronaut.run(Application.class)
    }
}