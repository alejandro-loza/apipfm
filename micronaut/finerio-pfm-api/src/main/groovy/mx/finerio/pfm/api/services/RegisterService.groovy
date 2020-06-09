package mx.finerio.pfm.api.services

import javax.validation.constraints.NotBlank

interface RegisterService {
    void register(String username, String rawPassword, List<String> authorities)
}