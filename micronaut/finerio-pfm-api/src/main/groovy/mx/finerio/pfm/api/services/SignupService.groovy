package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.dtos.resource.ClientDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.validation.SignupCommand

interface SignupService {

    @Log
    ClientDto create(SignupCommand dto ) throws Exception

}
