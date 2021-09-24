package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.NextCursorService
import mx.finerio.pfm.api.services.RequestLoggerService
import mx.finerio.pfm.api.validation.RequestLoggerFiltersCommand
import javax.annotation.Nullable
import javax.inject.Inject

@Controller("/requestLogger")
@Validated
@Secured('isAuthenticated()')
class RequestLoggerController {

    @Inject
    RequestLoggerService requestLoggerService

    @Inject
    NextCursorService nextCursorService

    @Log
    @Get("{?dateFrom,dateTo,userId,eventType,cursor}")
    @Transactional
    Single<ResourcesDto> findAllByFilter(@Nullable Long dateFrom,
                                         @Nullable Long dateTo,
                                         @Nullable Long userId,
                                         @Nullable String eventType,
                                         @Nullable Long cursor){
        RequestLoggerFiltersCommand cmd = new RequestLoggerFiltersCommand()
        cmd.userId = userId
        cmd.dateFrom = dateFrom
        cmd.dateTo = dateTo
        cmd.eventType = eventType
        cmd.cursor = cursor
        nextCursorService.generateResourcesDto(
                requestLoggerService.findByFilters(cmd))

    }
}
