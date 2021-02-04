package mx.finerio.pfm.api.controllers.handlers

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.web.router.exceptions.UnsatisfiedQueryValueRouteException
import mx.finerio.pfm.api.dtos.utilities.ErrorDto
import mx.finerio.pfm.api.dtos.utilities.ErrorsDto
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QueryValueExceptionHandler
    implements ExceptionHandler<UnsatisfiedQueryValueRouteException,
    HttpResponse> {

  @Inject
  MessageService messageService

  @Override
  HttpResponse handle( HttpRequest request,
      UnsatisfiedQueryValueRouteException e ) { 

    def message = 'url.query.value.invalid'
    def error = new ErrorDto()
    error.code = message
    error.title = messageService.getMessage( message )
    error.detail = messageService.getMessage( "${message}.detail" )
    def errors = new ErrorsDto()
    errors.errors = [ error ]
    return HttpResponse.badRequest( errors )

  }

}
