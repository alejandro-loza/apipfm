package mx.finerio.pfm.api.controllers.handlers


import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.JsonExceptionHandler
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Replaces(JsonExceptionHandler)
class NotFoundExceptionHandler  implements ExceptionHandler<ItemNotFoundException, HttpResponse>{

    @Inject
    MessageService messageService

    @Override
    HttpResponse handle(HttpRequest request, ItemNotFoundException ex ) {
        def message = ex.message
        ErrorDto error = new ErrorDto()
        error.with {
            code = message
            title = messageService.getMessage(message)
            detail = messageService.getMessage( "${message}.detail" )
        }
        ErrorsDto errorsDto = new ErrorsDto()
        errorsDto.errors = [error]
        return HttpResponse.notFound().body(errorsDto)
    }

}
