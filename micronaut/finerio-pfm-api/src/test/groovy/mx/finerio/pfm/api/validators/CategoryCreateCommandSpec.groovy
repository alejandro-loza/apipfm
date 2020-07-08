package mx.finerio.pfm.api.validators


import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import spock.lang.Specification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

@MicronautTest(application = Application.class)
class CategoryCreateCommandSpec extends Specification{
    Validator validator

    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    def "Should validate a category command"(){
        given:'an category command validator'
        CategoryCreateCommand cmd = new CategoryCreateCommand()
        cmd.with {
            userId = 123
            name = "Ropa y Calzado"
            color = "#00FFAA"
            parentCategoryId = 123
        }
        when:
        Set<ConstraintViolation<CategoryCreateCommand>> violations = validator.validate(cmd)

        then:
        assert violations.isEmpty()
    }

    def "Should not validate a account command"(){
        given:'an account command validator'
        CategoryCreateCommand cmd = new CategoryCreateCommand()

        when:
        Set<ConstraintViolation<CategoryCreateCommand>> violations = validator.validate(cmd)

        then:
        assert !violations.isEmpty()
        violations.size() == 3
    }

}
