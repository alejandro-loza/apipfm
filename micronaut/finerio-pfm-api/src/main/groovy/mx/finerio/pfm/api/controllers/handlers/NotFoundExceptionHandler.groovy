package mx.finerio.pfm.api.controllers.handlers

import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.JsonExceptionHandler
import mx.finerio.pfm.api.dtos.Error
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject
import javax.inject.Singleton

@Produces
@Singleton
@Requires(classes = [JsonProcessingException, ExceptionHandler])
@Replaces(JsonExceptionHandler)
class NotFoundExceptionHandler  implements ExceptionHandler<NotFoundException, HttpResponse>{

    @Override
    HttpResponse handle( HttpRequest request, NotFoundException ex ) {
        return HttpResponse.notFound().body(ex.message)
    }

}
