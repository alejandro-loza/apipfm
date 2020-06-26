package mx.finerio.pfm.api.validators


import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.TransactionCreateCommand
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@MicronautTest(application = Application.class)
class TransactionCommandSpec extends Specification{
    Validator validator

    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    def "Should validate a transaction command"(){
        given:'an transaction command validator'
        TransactionCreateCommand cmd = new TransactionCreateCommand()
        cmd.with {
            accountId = 1
            date = new Date().getTime()
            charge = true
            description = 'wild description appears'
            amount = 100.00
        }
        when:
        Set<ConstraintViolation<AccountCreateCommand>> violations = validator.validate(cmd)

        then:
        assert violations.isEmpty()
    }

    def "Should not validate a account command"(){
        given:'an account command validator'
        TransactionCreateCommand cmd = new TransactionCreateCommand()

        when:
        Set<ConstraintViolation<AccountCreateCommand>> violations = validator.validate(cmd)

        then:
        assert !violations.isEmpty()
        violations.size() == 5
    }


}
