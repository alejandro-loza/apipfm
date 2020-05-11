package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.User

interface UserService {
    User getUser(long id)
}