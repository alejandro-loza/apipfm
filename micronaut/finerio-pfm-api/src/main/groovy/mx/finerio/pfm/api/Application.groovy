package mx.finerio.pfm.api

import groovy.transform.CompileStatic
import io.micronaut.runtime.Micronaut
import mx.finerio.pfm.api.services.RegisterService

import javax.inject.Singleton
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent

@CompileStatic
@Singleton
class Application implements ApplicationEventListener<ServerStartupEvent> {

    protected final RegisterService registerService

    Application(RegisterService registerService) {
        this.registerService = registerService
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        registerService.register("sherlock", 'elementary', ['ROLE_DETECTIVE'])
    }

    static void main(String[] args) {
        Micronaut.run(Application.class)
    }
}