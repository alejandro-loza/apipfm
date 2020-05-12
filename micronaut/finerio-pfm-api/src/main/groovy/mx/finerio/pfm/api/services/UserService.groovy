package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.validation.UserCreateCommand

interface UserService {
    User getUser(long id)
    User create(UserCreateCommand cmd)
    User update(UserCreateCommand cmd, Long id)
    void delete(Long id)
    List<UserDto> findAllByCursor(long cursor)
    List<UserDto> getAll()
}