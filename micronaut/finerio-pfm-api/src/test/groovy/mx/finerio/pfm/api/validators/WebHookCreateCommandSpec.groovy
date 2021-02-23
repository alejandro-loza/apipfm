package mx.finerio.pfm.api.validators

import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.WebHookCreateCommand
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@MicronautTest(application = Application.class)
class WebHookCreateCommandSpec extends Specification {
    Validator validator

    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    def "Should validate a webhook command"(){
        given:'an webhook command validator'
        WebHookCreateCommand cmd = new WebHookCreateCommand()
        cmd.with {
            nature == 'test'
            url == 'www.test.com'
        }
        when:
        Set<ConstraintViolation<WebHookCreateCommand>> violations = validator.validate(cmd)

        then:
        assert violations.isEmpty()
    }
}
