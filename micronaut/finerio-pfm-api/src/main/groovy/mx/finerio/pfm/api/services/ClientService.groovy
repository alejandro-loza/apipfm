package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Client

interface ClientService {
    Client register(String username, String rawPassword, List<String> authorities)
    Client findByUsername(String username)
}