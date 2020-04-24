package mx.finerio.pfm.api.services.imp

import io.micronaut.http.annotation.Body
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.pogos.UserCreateCommand
import mx.finerio.pfm.api.repository.UserRepository
import mx.finerio.pfm.api.services.UserService

import javax.inject.Singleton
import javax.validation.Valid

@Singleton
class UserServiceImp implements UserService {
    protected final UserRepository userRepository

    UserServiceImp(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    User create(@Body @Valid UserCreateCommand cmd) {
        return userRepository.save(cmd)
    }

}
