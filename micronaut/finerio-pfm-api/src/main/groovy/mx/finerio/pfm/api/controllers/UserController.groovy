package mx.finerio.pfm.api.controllers

import io.micronaut.context.MessageSource
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.hateoas.JsonError
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.dtos.UserErrorDto
import mx.finerio.pfm.api.dtos.ResourcesResponseDto
import mx.finerio.pfm.api.exceptions.UserNotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.UserServiceRepository
import mx.finerio.pfm.api.validation.UserCreateCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.constraints.NotNull



@Controller("/users")
@Validated
class UserController {

    public static final int MAX_ROWS = 100

    @Inject
    UserServiceRepository userServiceRepository

    @Inject
    UserService userService

    @Inject
    MessageSource messageSource

    @Post("/")
    Single<UserDto> save(@Body @Valid UserCreateCommand cmd){
        Single.just(new UserDto(userServiceRepository.save(new User(cmd.name))))
    }

    @Get("/{id}")
    Single<UserDto> show(@NotNull Long id) {
        Single.just(new UserDto(userService.getUser(id)))
    }

    @Get("{?offset}")
    Single<Map> showAll(@Nullable String offset) {
        List<UserDto> users = userServiceRepository.findAll([offset: offset, max: MAX_ROWS]).collect { new UserDto(it) }
        Single.just(users.isEmpty() ? [] :  new ResourcesResponseDto(users)) as Single<Map>
    }

    @Put("/{id}")
    Single<UserDto> edit(@Body @Valid UserCreateCommand cmd, @NotNull Long id ) {
        User user = userService.getUser(id)
        user.with {
            name = cmd.name
        }
        Single.just(new UserDto(userServiceRepository.save(user)))
    }

    @Delete("/{id}")
    HttpResponse delete(@NotNull Long id) {
        userService.getUser(id)
        userServiceRepository.delete(id)
        return HttpResponse.noContent()
    }



    @Error
    HttpResponse<List<UserErrorDto>> jsonError(ConstraintViolationException constraintViolationException) {
        HttpResponse.<List<UserErrorDto>> status(HttpStatus.BAD_REQUEST,
                messageBuilder("system.json.invalid").get()).body(
                constraintViolationException.constraintViolations.collect {
                    new UserErrorDto(it.message, messageSource)
                })
    }

    @Error(status = HttpStatus.BAD_REQUEST)
    HttpResponse<JsonError> badRequest(HttpRequest request) {
        HttpResponse.<JsonError>badRequest().body(
                new JsonError(messageBuilder("user.malformed.message").orElse('Bad Request'))
        )
    }

    private Optional<String> messageBuilder(String code) {
        messageSource.getMessage(code, MessageSource.MessageContext.DEFAULT)
    }

    @Error(exception = UserNotFoundException)
    HttpResponse<JsonError> notFound(UserNotFoundException ex) {
        HttpResponse.<JsonError>notFound().body(new JsonError(ex.message))
    }
}
