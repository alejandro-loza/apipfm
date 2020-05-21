package mx.finerio.pfm.api.validators


import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.AccountCommand
import mx.finerio.pfm.api.validation.BudgetCommand
import mx.finerio.pfm.api.validation.TransactionCommand
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@MicronautTest(application = Application.class)
class BudgetCommandSpec extends Specification{
    Validator validator

    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    def "Should validate a budget command"(){
        given:'a budget command validator'
        BudgetCommand cmd = new BudgetCommand()
        cmd.with {
            userId = 1
            categoryId = 1
            name = 'awesome name'
            parentBudgetId = 1
            amount =100.00
        }
        when:
        Set<ConstraintViolation<AccountCommand>> violations = validator.validate(cmd)

        then:
        assert violations.isEmpty()
    }

    def "Should not validate a account command"(){
        given:'an account command validator'
        BudgetCommand cmd = new BudgetCommand()

        when:
        Set<ConstraintViolation<AccountCommand>> violations = validator.validate(cmd)

        then:
        assert !violations.isEmpty()
        violations.size() == 6
    }


}
