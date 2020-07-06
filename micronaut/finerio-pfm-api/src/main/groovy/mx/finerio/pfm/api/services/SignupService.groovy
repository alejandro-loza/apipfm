package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.ClientDto
import mx.finerio.pfm.api.validation.SignupCommand

interface SignupService {
    ClientDto create(SignupCommand dto ) throws Exception
}