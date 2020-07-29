package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.security.utils.SecurityService
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.services.imp.UserServiceImp
import mx.finerio.pfm.api.validation.UserCommand
import spock.lang.Specification

import java.security.Principal

import static java.util.Optional.of

@Property(name = 'spec.name', value = 'user service')
@MicronautTest(application = Application.class)
class UserServiceSpec extends Specification {

    UserService userService = new UserServiceImp()

    void setup(){
        userService.userGormService = Mock(UserGormService)
        userService.securityService = Mock(SecurityService)
        userService.clientService = Mock(ClientService)
    }

    def 'Should save an user'(){
        given:'a user command request body'
        UserCommand cmd = new UserCommand(name:"awesome name")

        def client = new Client()
        when:
        1 * userService.userGormService.save(_  as User) >> new User(cmd.name, client)

        def response = userService.create(cmd, client)

        then:
        response instanceof User
    }

    def 'Should throw exception on an username that already exist'(){
        given:'a user command request body'
        UserCommand cmd = new UserCommand(name:"awesome name")

        when:
        1 * userService.securityService.getAuthentication() >> of(Principal)
        1 * userService.clientService.findByUsername(_ as String) >>  new Client()
        1 * userService.userGormService.findByNameAndAndClientAndDateDeletedIsNull(_ as String, _ as Client) >> new User()
        0 * userService.userGormService.save(_  as User)

        userService.update(cmd, 1L)

        then:
        BadRequestException e = thrown()
        e.message == 'user.nonUnique'
    }

    def "Should throw exception on null body"() {

        when:
        userService.create(null, new Client())
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should throw exception on null body on update"() {

        when:
        userService.update(null, 1L)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should get a user"(){

        when:
        1 * userService.securityService.getAuthentication() >> of(Principal)
        1 * userService.clientService.findByUsername(_ as String) >>  new Client()
        1 * userService.userGormService.findByIdAndClientAndDateDeletedIsNull(_ as Long, _ as Client) >> new User()

        def result = userService.getUser(1L)

        then:
        result instanceof User
    }

    def "Should not get a user and throw exception"(){

        when:
        1 * userService.securityService.getAuthentication() >> of(Principal)
        1 * userService.clientService.findByUsername(_ as String) >>  new Client()
        1 * userService.userGormService.findByIdAndClientAndDateDeletedIsNull(_ as Long, _ as Client) >> null
        userService.getUser(666L)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'user.notFound'
    }


    def "Should get user by a cursor " () {

        when:
        1 * userService.userGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(_ as Long, _ as Map) >> [new User()]
        def response = userService.findAllByCursor(2)

        then:
        response instanceof  List<UserDto>
    }

}
