package mx.finerio.pfm.api.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.utils.SecurityService
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.validation.UserCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static io.reactivex.Single.just

@Controller("/users")
@Validated
@Secured('isAuthenticated()')
class UserController {

    @Inject
    UserService userService

    @Inject
    SecurityService securityService

    @Inject
    ClientService clientService

    @Log
    @Post("/")
    Single<UserDto> save(@Body @Valid UserCommand cmd){
        just(new UserDto(userService.create(cmd, getCurrenLoggedClient())))
    }

    @Log
    @Get("/{id}")
    Single<UserDto> show(@NotNull Long id) {
        just(new UserDto(userService.getUser(id)))
    }

    @Log
    @Get("{?cursor}")
    ResourcesDto showAll(@Nullable Long cursor) {
        List<UserDto> users = cursor ? userService.getAllByClientAndCursor(getCurrenLoggedClient(), cursor)
                :  userService.getAllByClient(getCurrenLoggedClient())
        new ResourcesDto(users)
    }

    @Log
    @Put("/{id}")
    Single<UserDto> edit(@Body @Valid UserCommand cmd, @NotNull Long id ) {
        just(new UserDto(userService.update(cmd,id)))
    }

    @Log
    @Delete("/{id}")
    HttpResponse delete(@NotNull Long id) {
        userService.delete(id)
        HttpResponse.noContent()
    }

    private Client getCurrenLoggedClient() {
        clientService.findByUsername(securityService.getAuthentication().get().name)
    }
}
