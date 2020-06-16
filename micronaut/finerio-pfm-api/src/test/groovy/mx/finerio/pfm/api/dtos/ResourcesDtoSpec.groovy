package mx.finerio.pfm.api.dtos

import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import spock.lang.Specification

@MicronautTest(application = Application.class)
class ResourcesDtoSpec extends Specification {

    def "should get null on empty list"(){
        setup:'an users dto'
        List<UserDto> users = []
        ResourcesDto userDto = new ResourcesDto(users)

        expect:
        !userDto.nextCursor
    }

    def "should get null on last id equals 1"(){
        setup:'an users dto with one user dto'
        def userDto1 = new UserDto()
        userDto1.id = 1

        List<UserDto> users = [userDto1]
        ResourcesDto usersDto = new ResourcesDto(users)

        expect:
        !usersDto.nextCursor
    }

    def "should get 1 on last id equals 2"(){
        setup:'an users dto with one user dto'
        def userDto3 = new UserDto()
        userDto3.id = 3

        def userDto2 = new UserDto()
        userDto2.id = 2

        List<UserDto> users = [userDto3,userDto2]
        ResourcesDto usersDto = new ResourcesDto(users)

        expect:
        usersDto.nextCursor == 1
    }
}
