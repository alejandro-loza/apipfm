package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.validation.UserCommand

interface UserService {
    User getUser(long id)
    User create(UserCommand cmd, Client client)
    User update(UserCommand cmd, Long id)
    void delete(Long id)
    List<UserDto> findAllByCursor(long cursor)
    List<UserDto> getAll()
}