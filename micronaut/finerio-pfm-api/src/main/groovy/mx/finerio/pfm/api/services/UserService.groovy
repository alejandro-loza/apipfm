package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.pogos.UserCreateCommand

interface UserService {
    User create(UserCreateCommand cmd)
}