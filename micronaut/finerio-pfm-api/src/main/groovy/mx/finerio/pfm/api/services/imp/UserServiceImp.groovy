package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.UserCommand
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service
class UserServiceImp extends ServiceTemplate implements UserService {

    @Inject
    UserGormService userGormService

    @Override
    UserDto getUser(long id) {
        new UserDto(findUser( id))
    }

    @Override
    User findUser(long id) {
        Optional.ofNullable(userGormService.findByIdAndClientAndDateDeletedIsNull(id, getCurrentLoggedClient()))
                .orElseThrow({ -> new ItemNotFoundException('user.notFound') })
    }

    @Override
    User create(UserCommand cmd, Client client) {
        verifyBody(cmd)
        verifyUnique(cmd, client)
        return userGormService.save(new User(cmd.name, client))
    }

    @Override
    @Transactional
    User update(UserCommand cmd, Long id){
        verifyBody(cmd)
        verifyUnique(cmd, getCurrentLoggedClient())
        User user = findUser(id)
        user.with {
            name = cmd.name
        }
        userGormService.save(user)
    }

    @Override
    void delete(User user){
        user.dateDeleted = new Date()
        userGormService.save(user)
    }

    @Override
    @Transactional
    List<UserDto> getAllByClient(Client client) {
        userGormService.findAllByClientAndDateDeletedIsNull(client, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{user -> new UserDto(user)}
    }

    @Override
    @Transactional
    List<UserDto> getAllByClientAndCursor(Client client, Long cursor) {
        userGormService.findAllByClientAndDateDeletedIsNullAndIdLessThanEquals(client,cursor,
                [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{user -> new UserDto(user)}
    }

    @Override
    List<UserDto> findAllByCursor(long cursor) {
        userGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new UserDto(it)}
    }

    private static void verifyBody(UserCommand cmd) {
        if (!cmd) {
            throw new IllegalArgumentException('request.body.invalid')
        }
    }

    private void verifyUnique(UserCommand cmd, Client client) {
        if (userGormService.findByNameAndAndClientAndDateDeletedIsNull(cmd.name, client)) {
            throw new BadRequestException('user.nonUnique')
        }
    }

}
