package mx.finerio.pfm.api.controllers.handlers

import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.ErrorsDto
import mx.finerio.pfm.api.services.MessageService

import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.netty.converters.UnsatisfiedRouteHandler
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Replaces(UnsatisfiedRouteHandler)
class UnsatisfiedRouteExceptionHandler
    implements ExceptionHandler<UnsatisfiedBodyRouteException, HttpResponse> {

  @Inject
  MessageService messageService

  @Override
  HttpResponse handle( HttpRequest request,
      UnsatisfiedBodyRouteException e ) {

    def message = 'json.body.empty'
    def error = new ErrorDto()
    error.code = message
    error.title = messageService.getMessage( message )
    error.detail = messageService.getMessage( "${message}.detail" )
    def errors = new ErrorsDto()
    errors.errors = [ error ]
    return HttpResponse.badRequest( errors )

  }

}
