package mx.finerio.pfm.api.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.pogos.UserCreateCommand
import mx.finerio.pfm.api.services.UserService

import javax.validation.Valid

@Controller("/users")
class UserController {
    protected final UserService userService

    UserController(UserService userService) {
        this.userService = userService
    }

    @Post("/")
    HttpResponse<User> save(@Body @Valid UserCreateCommand cmd) {
        User user = userService.create(cmd);
        HttpResponse
                .created(user)
                .headers({ headers -> headers.location(location(user.id)) })
    }

    protected static URI location(Long id) {
        URI.create("/genres/" + id)
    }

    protected static URI location(User user) {
        location(user.id)
    }

}
