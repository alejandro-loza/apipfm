package mx.finerio.pfm.api.validators

import com.sun.org.apache.bcel.internal.generic.DADD
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.AccountCommand
import mx.finerio.pfm.api.validation.TransactionCommand
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
        TransactionCommand cmd = new TransactionCommand()
        cmd.with {
            date = new Date()
            charge = true
            description = 'wild description appears'
            amount = 100.00
        }
        when:
        Set<ConstraintViolation<AccountCommand>> violations = validator.validate(cmd)

        then:
        assert violations.isEmpty()
    }

    def "Should not validate a account command"(){
        given:'an account command validator'
        TransactionCommand cmd = new TransactionCommand()

        when:
        Set<ConstraintViolation<AccountCommand>> violations = validator.validate(cmd)

        then:
        assert !violations.isEmpty()
        violations.size() == 4
    }


}
