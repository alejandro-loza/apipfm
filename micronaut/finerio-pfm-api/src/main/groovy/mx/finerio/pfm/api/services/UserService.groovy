package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.UserCommand

interface UserService {

    @Log
    User getUser(long id)

    @Log
    User create(UserCommand cmd, Client client)

    @Log
    User update(UserCommand cmd, Long id)

    @Log
    void delete(Long id)

    @Log
    List<UserDto> findAllByCursor(long cursor)

    @Log
    List<UserDto> getAllByClient(Client client)

    @Log
    List<UserDto> getAllByClientAndCursor(Client client, Long cursor)

}
