package mx.finerio.pfm.api.dtos

import io.micronaut.context.MessageSource
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(application = Application.class)
class UserErrorDtoSpec extends Specification {

    @Inject
    MessageSource messageSource

    def "Should return an user error message "(){
        given:
        UserErrorDto dto = new UserErrorDto('save.cmd.name: user.name.null', messageSource)

        expect:
        dto.with {
            assert title == 'User name is null'
            assert code == 'user.name.null'
            assert detail == 'The name of the user you provided was null. Please provide a valid one.'
        }

    }
}
