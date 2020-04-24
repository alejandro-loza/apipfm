package mx.finerio.pfm.api.repository

import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.pogos.UserCreateCommand

import javax.validation.constraints.NotBlank

interface UserRepository {
    User save(@NotBlank UserCreateCommand cmd)
}