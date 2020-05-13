package mx.finerio.pfm.api.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.context.MessageSource
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.UserNotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.validation.UserCreateCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static io.reactivex.Single.just

@Controller("/users")
@Validated
class UserController {

    @Inject
    UserService userService

    @Inject
    MessageSource messageSource

    @Post("/")
    Single<UserDto> save(@Body @Valid UserCreateCommand cmd){
        just(new UserDto(userService.create(cmd)))
    }

    @Get("/{id}")
    Single<UserDto> show(@NotNull Long id) {
        just(new UserDto(userService.getUser(id)))
    }

    @Get("{?cursor}")
    Single<Map> showAll(@Nullable Long cursor) {
        List<UserDto> users = cursor ? userService.findAllByCursor(cursor) : userService.getAll()
        just(users.isEmpty() ? [] :  new ResourcesDto(users)) as Single<Map>
    }

    @Put("/{id}")
    Single<UserDto> edit(@Body @Valid UserCreateCommand cmd, @NotNull Long id ) {
        just(new UserDto(userService.update(cmd,id)))
    }

    @Delete("/{id}")
    HttpResponse delete(@NotNull Long id) {
        userService.delete(id)
        HttpResponse.noContent()
    }

    @Error(exception = UserNotFoundException)
    HttpResponse notFound(UserNotFoundException ex) {
        HttpResponse.notFound().body(ex.message)
    }

}
