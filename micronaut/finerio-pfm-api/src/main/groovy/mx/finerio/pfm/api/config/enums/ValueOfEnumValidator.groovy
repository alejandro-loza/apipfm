package mx.finerio.pfm.api.config.enums

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {
    private List<String> acceptedValues;

    @Override
    void initialize(ValueOfEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass()
                .getEnumConstants() as Object[])
                .map(Enum.&name)
                .collect(Collectors.toList())
    }

    @Override
    boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true
        }

        return acceptedValues.contains(value.toString())
    }
}