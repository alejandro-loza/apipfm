package mx.finerio.pfm.api.services

import io.micronaut.aop.MethodInvocationContext
import mx.finerio.pfm.api.domain.RequestLogger

import java.lang.reflect.Method

interface RequestLoggerService {
    RequestLogger create(MethodInvocationContext<Object, Object> context, Object returnValue)
    String getFullMethodName(Method method )
}