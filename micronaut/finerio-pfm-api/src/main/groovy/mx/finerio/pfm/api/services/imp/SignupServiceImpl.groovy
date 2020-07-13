package mx.finerio.pfm.api.services.imp

import io.micronaut.security.authentication.providers.PasswordEncoder
import io.micronaut.spring.tx.annotation.Transactional
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.ClientProfile
import mx.finerio.pfm.api.dtos.ClientDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.services.SignupService
import mx.finerio.pfm.api.services.gorm.ClientGormService
import mx.finerio.pfm.api.services.gorm.ClientProfileGormService
import mx.finerio.pfm.api.services.gorm.ClientRoleGormService
import mx.finerio.pfm.api.services.gorm.RoleGormService
import mx.finerio.pfm.api.validation.SignupCommand

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignupServiceImpl implements SignupService {

    @Inject
    ClientGormService clientGormService

    @Inject
    ClientProfileGormService clientProfileGormService

    @Inject
    ClientRoleGormService clientRoleGormService

    @Inject
    RoleGormService roleGormService

    @Inject
    PasswordEncoder passwordEncoder

    @Override
    @Transactional
    ClientDto create(SignupCommand dto ) throws Exception {
        Client client = createClient( dto )
        createClientDto( client, createClientProfile( dto, client ) )
    }

    private Client createClient(SignupCommand dto ) throws Exception {
        if ( !dto ) {
            throw new IllegalArgumentException( 'signupService.create.dto.null' )
        }

        if ( clientGormService.findByUsername( dto.username )) {
            throw new BadRequestException( 'signup.username.exists' )
        }

        Client client = clientGormService.save( dto.username, passwordEncoder.encode( dto.password ) )
        clientRoleGormService.save( client, roleGormService.find( 'ROLE_CLIENT' ) )
        client
    }

    private ClientProfile createClientProfile(SignupCommand dto,
                                              Client client ) throws Exception {

        ClientProfile clientProfile = new ClientProfile()
        clientProfile.client = client
        clientProfile.name = dto.name
        clientProfile.firstLastName = dto.firstLastName
        clientProfile.secondLastName = dto.secondLastName
        clientProfile.email = dto.email
        clientProfile.companyName = dto.companyName
        clientProfileGormService.save( clientProfile )
    }

    private static ClientDto createClientDto(Client client,
                                             ClientProfile clientProfile ) throws Exception {

        ClientDto clientDto = new ClientDto()
        clientDto.with {
            id = client.id
            name = clientProfile.name
            firstLastName = clientProfile.firstLastName
            secondLastName = clientProfile.secondLastName
            email = clientProfile.email
            companyName = clientProfile.companyName
            username = client.username
        }
        clientDto
    }

}
