package mx.finerio.pfm.api.controllers.handlers


import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.JsonExceptionHandler
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Replaces(JsonExceptionHandler)
class NotFoundExceptionHandler  implements ExceptionHandler<NotFoundException, HttpResponse>{

    @Inject
    MessageService messageService

    @Override
    HttpResponse handle( HttpRequest request, NotFoundException ex ) {
        return HttpResponse.notFound().body(messageService.getMessage( ex.message ))
    }

}
