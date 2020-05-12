package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.exceptions.UserNotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.UserServiceRepository
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service
class UserCommonsServiceImp implements UserService {

    @Inject
    UserServiceRepository userService

    @Override
    User getUser(long id) {
        Optional.ofNullable(userService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new UserNotFoundException('The user ID you requested was not found.') })
    }
}
