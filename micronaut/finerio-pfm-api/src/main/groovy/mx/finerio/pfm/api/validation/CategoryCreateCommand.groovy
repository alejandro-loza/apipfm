package mx.finerio.pfm.api.validation

import groovy.transform.ToString

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ToString(includeNames = true, includePackage = false)
class CategoryCreateCommand extends ValidationCommand {

    Long userId

    @NotNull(message= 'category.name.null')
    @NotBlank(message= 'category.name.blank')
    String name

    String color

    Long parentCategoryId
}
