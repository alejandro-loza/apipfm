package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.logging.Hidden
import mx.finerio.pfm.api.logging.Log

interface ClientService {

    @Log
    Client register(String username, @Hidden String rawPassword,
        List<String> authorities)

    @Log
    Client findByUsername(String username)
}
