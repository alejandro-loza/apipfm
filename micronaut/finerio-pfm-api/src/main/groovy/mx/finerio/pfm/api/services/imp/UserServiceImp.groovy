package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.UserNotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.UserCreateCommand
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service
class UserServiceImp implements UserService {

    public static final int MAX_ROWS = 100

    @Inject
    UserGormService userGormService

    @Override
    User getUser(long id) {
        Optional.ofNullable(userGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new UserNotFoundException('The user ID you requested was not found.') })
    }

    @Override
    User create(UserCreateCommand cmd){
        userGormService.save(new User(cmd.name))
    }

    @Override
    User update(UserCreateCommand cmd, Long id){
        User user = getUser(id)
        user.with {
            name = cmd.name
        }
        userGormService.save(user)
    }

    @Override
    void delete(Long id){
        User user = getUser(id)
        user.dateDeleted = new Date()
        userGormService.save(user)
    }

    @Override
    List<UserDto> getAll() {
        userGormService.findAll([max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new UserDto(it)}
    }

    @Override
    List<UserDto> findAllByCursor(long cursor) {
        userGormService.findByIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new UserDto(it)}
    }
}
