package mx.finerio.pfm.api.controllers.handlers

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler

import io.micronaut.validation.exceptions.ConstraintExceptionHandler
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@Singleton
@Replaces(ConstraintExceptionHandler)
class ConstraintViolationExceptionHandler
        implements ExceptionHandler<ConstraintViolationException, HttpResponse>{

    @Inject
    MessageService messageService

    @Override
    HttpResponse handle( HttpRequest request,
                         ConstraintViolationException e ) {

        ErrorsDto errors = new ErrorsDto()
        errors.errors = e.constraintViolations.collect { ConstraintViolation violation ->
            def message = violation.message
            ErrorDto error = new ErrorDto()
            error.with {
                code = message
                title = messageService.getMessage( message )
                detail = messageService.getMessage( "${message}.detail" )
            }
            error
        }

        HttpResponse.badRequest( errors )
    }

}
