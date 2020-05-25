package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.UserCommand
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
                .orElseThrow({ -> new NotFoundException('The user ID you requested was not found.') })
    }

    @Override
    User create(UserCommand cmd){
        if ( !cmd  ) {
            throw new IllegalArgumentException(
                    'request.body.invalid' )
        }
        userGormService.save(new User(cmd.name))
    }

    @Override
    User update(UserCommand cmd, Long id){
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
        userGormService.findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{user -> new UserDto(user)}
    }

    @Override
    List<UserDto> findAllByCursor(long cursor) {
        userGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new UserDto(it)}
    }
}
