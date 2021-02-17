package mx.finerio.pfm.api.services.imp

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.Role
import mx.finerio.pfm.api.services.ClientService
import mx.finerio.pfm.api.services.gorm.ClientGormService
import mx.finerio.pfm.api.services.gorm.ClientRoleGormService
import mx.finerio.pfm.api.services.gorm.RoleGormService
import mx.finerio.pfm.api.services.security.BCryptPasswordEncoderService

import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@CompileStatic
@Singleton
class ClientServiceImp implements ClientService {

    @Inject
    RoleGormService roleGormService
    @Inject
    ClientGormService clientGormService
    @Inject
    ClientRoleGormService clientRoleGormService
    @Inject
    BCryptPasswordEncoderService passwordEncoder


    @Transactional
    @Override
    Client register(@NotBlank String username, @NotBlank String rawPassword, List<String> authorities) {
        Client client = findByUsername(username)
        if ( !client ) {
            final String encodedPassword = passwordEncoder.encode(rawPassword)
            client = clientGormService.save(username, encodedPassword)
        }

        if ( client && authorities ) {
            createAuthorities(authorities, client)
        }
        client
    }

    @Transactional
    @Override
    Client findByUsername(String username) {
        clientGormService.findByUsername(username)
    }

    private void createAuthorities(List<String> authorities, Client client) {
        for (String authority : authorities) {
            Role role = roleGormService.find(authority)
            if (!role) {
                role = roleGormService.save(authority)
            }
            if (!clientRoleGormService.find(client, role)) {
                clientRoleGormService.save(client, role)
            }
        }
    }
}