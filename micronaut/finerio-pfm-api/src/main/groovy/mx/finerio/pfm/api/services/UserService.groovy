package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.logging.RequestLogger
import mx.finerio.pfm.api.validation.UserCommand

interface UserService {

    @RequestLogger
    @Log
    UserDto getUser(long id)

    @Log
    User findUser(long id)

    @RequestLogger
    @Log
    User create(UserCommand cmd, Client client)

    @RequestLogger
    @Log
    User update(UserCommand cmd, Long id)

    @RequestLogger
    @Log
    void delete(User user)

    @Log
    List<UserDto> findAllByCursor(long cursor)

    @Log
    List<UserDto> getAllByClient(Client client)

    @Log
    List<UserDto> getAllByClientAndCursor(Client client, Long cursor)

}
