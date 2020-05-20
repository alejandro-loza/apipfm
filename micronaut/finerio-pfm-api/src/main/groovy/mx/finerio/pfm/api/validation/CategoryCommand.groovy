package mx.finerio.pfm.api.validation

import groovy.transform.CompileStatic
import io.micronaut.core.annotation.Introspected
import mx.finerio.pfm.api.domain.User

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import java.awt.Color

@Introspected
@CompileStatic
class CategoryCommand {
    @NotNull(message= 'category.user.null')
    Long userId
    @NotNull(message= 'category.name.null')
    @NotBlank(message= 'category.name.blank')
    String name
    @NotNull(message= 'category.color.null')
    String color
    @NotNull(message= 'category.parentCategory.null')
    Integer parentCategoryId
}
