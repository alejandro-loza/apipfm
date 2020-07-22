package mx.finerio.pfm.api.validation

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class CategoryUpdateCommand extends ValidationCommand {
    Long userId
    String name
    String color
    Long parentCategoryId
}
