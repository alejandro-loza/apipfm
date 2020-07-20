package mx.finerio.pfm.api.dtos

import com.fasterxml.jackson.annotation.JsonInclude

import groovy.transform.ToString

import mx.finerio.pfm.api.domain.Category

@JsonInclude(JsonInclude.Include.ALWAYS)
@ToString(includeNames = true, includePackage = false,
    includeSuperProperties = true)
class CategoryDto extends ResourceDto{

    Long userId
    String name
    String color
    Long parentCategoryId

    CategoryDto() {}

    CategoryDto(Category category) {
        this.id = category.id
        this.userId = category?.user?.id
        this.name = category.name
        this.parentCategoryId = category?.parent?.id
        this.color = category.color
        this.dateCreated = category.dateCreated
        this.lastUpdated = category.lastUpdated
    }
}
