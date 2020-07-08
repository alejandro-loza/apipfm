package mx.finerio.pfm.api.validators


import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.AccountCreateCommand
import mx.finerio.pfm.api.validation.BudgetCreateCommand
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@MicronautTest(application = Application.class)
class BudgetCreateCommandSpec extends Specification{
    Validator validator

    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    def "Should validate a budget command"(){
        given:'a budget command validator'
        BudgetCreateCommand cmd = new BudgetCreateCommand()
        cmd.with {
            userId = 1
            categoryId = 1
            name = 'awesome name'
            parentBudgetId = 1
            amount =100.00
        }
        when:
        Set<ConstraintViolation<AccountCreateCommand>> violations = validator.validate(cmd)

        then:
        assert violations.isEmpty()
    }

    def "Should not validate a account command"(){
        given:'an account command validator'
        BudgetCreateCommand cmd = new BudgetCreateCommand()

        when:
        Set<ConstraintViolation<AccountCreateCommand>> violations = validator.validate(cmd)

        then:
        assert !violations.isEmpty()
        violations.size() == 6
    }


}
