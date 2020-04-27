package mx.finerio.pfm.api.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.reactivex.Single
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.pogos.UserCreateCommand
import mx.finerio.pfm.api.services.UserService

import javax.inject.Inject
import javax.validation.Valid

@Controller("/users")
class UserController {
    @Inject
    UserService userService

    @Post("/")
    Single<UserDto> save(@Body @Valid UserCreateCommand cmd){
        Single.just(new UserDto(userService.save(new User(cmd))))
    }

}
