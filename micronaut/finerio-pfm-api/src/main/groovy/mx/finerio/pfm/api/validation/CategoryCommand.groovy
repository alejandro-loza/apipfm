package mx.finerio.pfm.api.validation

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull


class CategoryCommand extends ValidationCommand {

    Long userId

    @NotNull(message= 'category.name.null')
    @NotBlank(message= 'category.name.blank')
    String name

    @NotNull(message= 'category.color.null')
    String color

    Long parentCategoryId
}
