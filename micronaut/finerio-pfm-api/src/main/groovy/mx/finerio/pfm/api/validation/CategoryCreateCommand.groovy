package mx.finerio.pfm.api.validation

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class CategoryCreateCommand extends ValidationCommand {

    Long userId

    @NotNull(message= 'category.name.null')
    @NotBlank(message= 'category.name.blank')
    String name

    String color

    Long parentCategoryId
}
