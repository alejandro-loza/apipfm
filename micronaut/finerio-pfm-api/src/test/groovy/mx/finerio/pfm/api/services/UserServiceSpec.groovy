package mx.finerio.pfm.api.services


import mx.finerio.pfm.api.domain.Budget
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.BudgetDto
import mx.finerio.pfm.api.dtos.UserDto
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.services.imp.UserServiceImp
import mx.finerio.pfm.api.validation.BudgetCommand
import mx.finerio.pfm.api.validation.UserCommand
import spock.lang.Specification

class UserServiceSpec extends Specification {

    UserService userService = new UserServiceImp()

    void setup(){
        userService.userGormService = Mock(UserGormService)
    }

    def 'Should save an user'(){
        given:'a user command request body'
        UserCommand cmd = new UserCommand(name:"awesome name")

        when:
        1 * userService.userGormService.save(_  as User) >> new User(cmd.name)

        def response = userService.create(cmd)

        then:
        response instanceof User
    }

    def "Should throw exception on null body"() {

        when:
        userService.create(null)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should get a user"(){

        when:
        1 * userService.userGormService.findByIdAndDateDeletedIsNull(_ as Long) >> new User()

        def result = userService.getUser(1L)

        then:
        result instanceof User
    }

    def "Should not get a user and throw exception"(){

        when:
        1 * userService.userGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        userService.getUser(666)

        then:
        NotFoundException e = thrown()
        e.message == 'The user ID you requested was not found.'
    }

    def "Should get all user" () {
        when:
        1 * userService.userGormService.findAllByDateDeletedIsNull(_ as Map) >> [new User()]
        def response = userService.getAll()

        then:
        assert response instanceof  List<User>
    }

    def "Should not get all user" () {
        when:
        1 * userService.userGormService.findAllByDateDeletedIsNull(_ as Map) >> []
        def response = userService.getAll()

        then:
        response instanceof  List<UserDto>
        response.isEmpty()
    }

    def "Should get user by a cursor " () {

        when:
        1 * userService.userGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(_ as Long, _ as Map) >> [new User()]
        def response = userService.findAllByCursor(2)

        then:
        response instanceof  List<UserDto>
    }

}
