package mx.finerio.pfm.api.logging

import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.ApplicationContext
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.discovery.event.ServiceStartedEvent
import mx.finerio.pfm.api.dtos.resource.RequestLoggerDto
import mx.finerio.pfm.api.services.RequestLoggerService
import mx.finerio.pfm.api.services.imp.ServiceTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject
import javax.inject.Singleton
import java.lang.reflect.Method

@Singleton
class RequestLogerInterceptor  extends ServiceTemplate
        implements MethodInterceptor<Object, Object>, ApplicationEventListener<ServiceStartedEvent> {

    @Inject
    ApplicationContext applicationContext

    @Inject
    RequestLoggerService requestLoggerService

    private final static Logger log = LoggerFactory.getLogger(RequestLogerInterceptor)

    @Override
    Object intercept( MethodInvocationContext<Object, Object> context ) {

        Object returnValue = null
        try {
            returnValue = context.proceed()
            requestLoggerService.create(context, returnValue)

        } catch( Exception e ) {
            log.info( "{} >> {} - {}",
                    requestLoggerService.getFullMethodName( context.targetMethod ),
                    e.class.simpleName, e.message )
            throw e
        }
        returnValue
    }

    @Override
    void onApplicationEvent(ServiceStartedEvent event) {
        requestLoggerService = applicationContext.getBean(
                Class.forName('mx.finerio.pfm.api.services.imp.RequestLoggerServiceImp' ))
    }
}