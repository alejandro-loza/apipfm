package mx.finerio.pfm.api.config

import groovy.transform.CompileStatic
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import mx.finerio.pfm.api.services.RegisterService

import javax.inject.Singleton

@CompileStatic
@Singleton
class GenerateDefaultClient implements ApplicationEventListener<ServerStartupEvent> {
    protected final RegisterService registerService

    GenerateDefaultClient(RegisterService registerService) {
        this.registerService = registerService
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        registerService.register("sherlock", 'elementary', ['ROLE_DETECTIVE'])
    }

}