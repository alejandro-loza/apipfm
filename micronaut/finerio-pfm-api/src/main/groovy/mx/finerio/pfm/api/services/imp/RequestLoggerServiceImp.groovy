package mx.finerio.pfm.api.services.imp

import io.micronaut.aop.MethodInvocationContext
import mx.finerio.pfm.api.domain.RequestLogger
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.services.RequestLoggerService
import mx.finerio.pfm.api.services.gorm.RequestLoggerGormService

import javax.inject.Inject
import java.lang.reflect.Method

class RequestLoggerServiceImp implements RequestLoggerService{

    @Inject
    RequestLoggerGormService requestLoggerGormService


    @Override
    RequestLogger create(MethodInvocationContext<Object, Object> context, returnValue) {
        def functionMap  = [
               'UserServiceImp.create': userSaveFunction,
               'UserServiceImp.delete': userSaveFunction
        ]

        String eventName = getFullMethodName(context.targetMethod)
        RequestLogger request = new RequestLogger()

        request.with {
            user = functionMap[eventName](returnValue) as User
            request.eventType = eventName
        }
        requestLoggerGormService.save(request)

    }

    @Override
    String getFullMethodName(Method method ) {

        def clazz = method.declaringClass
        def className = clazz.simpleName
        def methodName = method.name
        return "${className}.${methodName}" //todo to enum
    }

    def userSaveFunction = { Object object ->
        object instanceof User ? object : object
    }

}
