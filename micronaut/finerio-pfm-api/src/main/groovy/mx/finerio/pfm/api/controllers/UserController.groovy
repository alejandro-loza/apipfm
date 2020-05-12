package mx.finerio.pfm.api.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.context.MessageSource
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.exceptions.UserNotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.UserServiceRepository
import mx.finerio.pfm.api.validation.UserCreateCommand
import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static io.reactivex.Single.just

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
        just(new UserDto(userServiceRepository.save(new User(cmd.name))))
    }

    @Get("/{id}")
    Single<UserDto> show(@NotNull Long id) {
        just(new UserDto(userService.getUser(id)))
    }

    @Get("{?cursor}")
    Single<Map> showAll(@Nullable Long cursor) {
        List<UserDto> users = cursor ? findAllByCursor(cursor) : findAll()
        just(users.isEmpty() ? [] :  new ResourcesDto(users)) as Single<Map>
    }

    @Put("/{id}")
    Single<UserDto> edit(@Body @Valid UserCreateCommand cmd, @NotNull Long id ) {
        User user = userService.getUser(id)
        user.with {
            name = cmd.name
        }
        just(new UserDto(userServiceRepository.save(user)))
    }

    @Delete("/{id}")
    HttpResponse delete(@NotNull Long id) {
        User user = userService.getUser(id)
        user.dateDeleted = new Date()
        userServiceRepository.save(user)//todo put it on service
        HttpResponse.noContent()
    }

    @Error
    HttpResponse<List<ErrorDto>> jsonError(ConstraintViolationException constraintViolationException) {
        HttpResponse.<List<ErrorDto>> status(HttpStatus.BAD_REQUEST,
                messageBuilder("request.body.invalid.title").get()).body(
                constraintViolationException.constraintViolations.collect {
                    new ErrorDto(it.message, messageSource)
                })
    }

    @Error(status = HttpStatus.BAD_REQUEST)
    HttpResponse<ErrorDto> badRequest(HttpRequest request) {
        HttpResponse.<ErrorDto>badRequest().body(
                new ErrorDto('request.body.invalid', this.messageSource)
        )
    }

    @Error(exception = UserNotFoundException)
    HttpResponse notFound(UserNotFoundException ex) {
        HttpResponse.notFound().body(ex.message)
    }

    @Error(exception = JsonProcessingException)
    HttpResponse<ErrorDto> badRequest(JsonProcessingException ex) {
        badRequestResponse()
    }

    @Error(exception = ConversionErrorException)
    HttpResponse<ErrorDto> badRequest(ConversionErrorException ex) {
        badRequestResponse()
    }

    private MutableHttpResponse<ErrorDto> badRequestResponse() {
        HttpResponse.<ErrorDto> badRequest().body(new ErrorDto('request.body.invalid', this.messageSource))
    }

    private Optional<String> messageBuilder(String code) {
        messageSource.getMessage(code, MessageSource.MessageContext.DEFAULT)
    }

    private List<UserDto> findAll() {
        userServiceRepository.findAll([max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new UserDto(it)}
    }

    private List<UserDto> findAllByCursor(long cursor) {
        userServiceRepository.findByIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new UserDto(it)}
    }

}
