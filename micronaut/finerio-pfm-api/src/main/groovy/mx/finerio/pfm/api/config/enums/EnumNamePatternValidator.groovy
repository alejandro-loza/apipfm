package mx.finerio.pfm.api.config.enums

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class EnumNamePatternValidator implements ConstraintValidator<EnumNamePattern, Enum<?>> {
    private Pattern pattern;

    @Override
    void initialize(EnumNamePattern constraintAnnotation) {
        try {
            pattern = Pattern.compile(constraintAnnotation.regexp());
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Given regex is invalid", e);
        }
    }

    @Override
    boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true
        }

        Matcher m = pattern.matcher(value.name())
        return m.matches();
    }
}