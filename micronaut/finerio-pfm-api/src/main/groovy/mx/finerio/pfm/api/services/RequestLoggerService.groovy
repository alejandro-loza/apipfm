package mx.finerio.pfm.api.services

import io.micronaut.aop.MethodInvocationContext
import mx.finerio.pfm.api.domain.RequestLogger
import mx.finerio.pfm.api.dtos.utilities.RequestLoggerDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.RequestLoggerFiltersCommand

import java.lang.reflect.Method

interface RequestLoggerService {
    @Log
    RequestLogger create(MethodInvocationContext<Object, Object> context, Object returnValue)

    @Log
    String getFullMethodName(Method method )

    @Log
    List<RequestLoggerDto> findByFilters(RequestLoggerFiltersCommand args)

}