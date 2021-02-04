package mx.finerio.pfm.api.controllers.handlers

import io.micronaut.context.annotation.Replaces
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ConversionErrorHandler
import io.micronaut.http.server.exceptions.ExceptionHandler
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Replaces(ConversionErrorHandler)
class ConversionErrorExceptionHandler
        implements ExceptionHandler<ConversionErrorException, HttpResponse>{

    @Inject
    MessageService messageService

    @Override
    HttpResponse handle( HttpRequest request, ConversionErrorException e ) {
        String message = 'request.body.invalid'
        ErrorDto error = new ErrorDto()
        error.with {
            code = message
            title = messageService.getMessage(message)
            detail = messageService.getMessage( "${message}.detail" )
        }

        ErrorsDto errors = new ErrorsDto()
        errors.errors = [ error ]
        HttpResponse.badRequest( errors )
    }

}
