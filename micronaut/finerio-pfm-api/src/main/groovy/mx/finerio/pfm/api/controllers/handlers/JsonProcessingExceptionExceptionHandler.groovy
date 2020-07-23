package mx.finerio.pfm.api.controllers.handlers

import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.JsonExceptionHandler
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.ErrorsDto
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [JsonProcessingException, ExceptionHandler])
@Replaces(JsonExceptionHandler)
class JsonProcessingExceptionExceptionHandler implements ExceptionHandler<JsonProcessingException, HttpResponse> {

    @Inject
    MessageService messageService

    @Override
    HttpResponse handle(HttpRequest request, JsonProcessingException exception) {
        def message = 'request.body.invalid'
        ErrorDto error = new ErrorDto()
        error.with {
            code = message
            title = messageService.getMessage( message )
            detail = messageService.getMessage( "${message}.detail" )
        }
        ErrorsDto errorsDto = new ErrorsDto()
        errorsDto.errors = [error]
        return HttpResponse.badRequest().body(errorsDto)
    }
}
