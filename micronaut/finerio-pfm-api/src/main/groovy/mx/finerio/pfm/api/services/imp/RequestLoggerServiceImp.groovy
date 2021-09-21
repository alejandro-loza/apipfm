package mx.finerio.pfm.api.services.imp

import io.micronaut.aop.MethodInvocationContext
import mx.finerio.pfm.api.domain.RequestLogger
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.services.RequestLoggerService
import mx.finerio.pfm.api.services.gorm.RequestLoggerGormService
import mx.finerio.pfm.api.services.gorm.UserGormService

import javax.inject.Inject
import java.lang.reflect.Method

class RequestLoggerServiceImp implements RequestLoggerService{

    @Inject
    RequestLoggerGormService requestLoggerGormService

    @Inject
    UserGormService userGormService

    @Override
    RequestLogger create(MethodInvocationContext<Object, Object> context, returnValue) {
        def functionMap  = [
               'UserServiceImp.create': userSaveFunction,
               'UserServiceImp.delete': userSaveFunction,
               'UserServiceImp.getUser': userSaveFunction,
               'UserServiceImp.update': userSaveFunction,
        ]

        String eventName = getFullMethodName(context.targetMethod)
        RequestLogger request = new RequestLogger()

        request.with {
            user = functionMap[eventName](['returnValue' :returnValue, 'parameters': context.parameters]) as User
            request.eventType = eventName
        }
        return requestLoggerGormService.save(request)
    }

    @Override
    String getFullMethodName(Method method ) {

        def clazz = method.declaringClass
        def className = clazz.simpleName
        def methodName = method.name
        return "${className}.${methodName}"
    }

    def userSaveFunction = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof User) {
          return  object.returnValue
        }
        if(object.returnValue != null && object.returnValue instanceof UserDto) {
            return  userGormService.findById(object.returnValue.id)
        }
        if(object.parameters.values()[0]?.value instanceof User){
            return object.parameters.values()[0].value
        }
        return
    }

}
