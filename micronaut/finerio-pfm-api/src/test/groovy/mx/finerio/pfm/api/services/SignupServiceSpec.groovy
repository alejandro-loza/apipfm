package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.ClientProfile
import mx.finerio.pfm.api.domain.ClientRole
import mx.finerio.pfm.api.domain.Role
import mx.finerio.pfm.api.dtos.resource.ClientDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.services.gorm.ClientGormService
import mx.finerio.pfm.api.services.gorm.ClientProfileGormService
import mx.finerio.pfm.api.services.gorm.ClientRoleGormService
import mx.finerio.pfm.api.services.gorm.RoleGormService
import mx.finerio.pfm.api.services.imp.SignupServiceImpl
import mx.finerio.pfm.api.validation.SignupCommand
import io.micronaut.security.authentication.providers.PasswordEncoder
import spock.lang.Specification

class SignupServiceSpec extends Specification {

    def service = new SignupServiceImpl()


    def setup() {

        service.clientGormService =  Mock( ClientGormService )
        service.clientProfileGormService =  Mock( ClientProfileGormService )
        service.clientRoleGormService = Mock( ClientRoleGormService )
        service.roleGormService = Mock( RoleGormService )
        service.passwordEncoder = Mock( PasswordEncoder )
    }

    def 'method worked successfully'() {

        given:
        def dto = new SignupCommand()
        dto.username = 'username'
        dto.password = 'password'
        when:
        def result = service.create( dto )
        then:
        1 * service.clientGormService.findByUsername( _ as String ) >> null
        1 * service.passwordEncoder.encode( _ as String ) >> 'myPassword'
        1 * service.clientGormService.save( _ as String, _ as String ) >>
                new Client()
        1 * service.roleGormService.find( _ as String ) >> new Role()
        1 * service.clientRoleGormService.save( _ as Client, _ as Role ) >>
                new ClientRole()
        1 * service.clientProfileGormService.save( _ as ClientProfile ) >>
                new ClientProfile()
        result instanceof ClientDto

    }

    def 'client username already exists'() {

        given:
        def dto = new SignupCommand()
        dto.username = 'username'

        dto.password = 'password'
        when:
        service.create( dto )
        then:
        1 * service.clientGormService.findByUsername( _ as String ) >> new Client()
        BadRequestException e = thrown()
        e.message == 'signup.username.exists'

    }

    def "parameter 'dto' is null"() {

        given:
        def dto = null
        when:
        service.create( dto )
        then:
        IllegalArgumentException e = thrown()
        e.message == 'signup.create.dto.null'

    }
}
