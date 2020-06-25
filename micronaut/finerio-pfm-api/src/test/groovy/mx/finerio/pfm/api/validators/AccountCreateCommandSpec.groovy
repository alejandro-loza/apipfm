package mx.finerio.pfm.api.validators

import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.AccountCreateCommand
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@MicronautTest(application = Application.class)
class AccountCreateCommandSpec extends Specification{
    Validator validator

    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    def "Should validate a account command"(){
        given:'an account command validator'
        AccountCreateCommand cmd = new AccountCreateCommand()
        cmd.with {
            userId = 1
            financialEntityId = 1
            nature = 'TEST'
            name = 'NAME'
            number = 1234123412341234
            balance = 0.01
        }
        when:
        Set<ConstraintViolation<AccountCreateCommand>> violations = validator.validate(cmd)

        then:
        assert violations.isEmpty()
    }

    def "Should not validate a account command"(){
        given:'an account command validator'
        AccountCreateCommand cmd = new AccountCreateCommand()

        when:
        Set<ConstraintViolation<AccountCreateCommand>> violations = validator.validate(cmd)

        then:
        assert !violations.isEmpty()
        violations.size() == 7
    }


}
