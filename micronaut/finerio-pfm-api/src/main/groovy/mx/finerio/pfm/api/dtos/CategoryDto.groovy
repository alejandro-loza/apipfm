package mx.finerio.pfm.api.dtos

import mx.finerio.pfm.api.domain.Category

class CategoryDto extends ResourceDto{

    Long userId
    String name
    String color
    Long parentCategoryId

    CategoryDto() {}

    CategoryDto(Category category) {
        this.id = category.id
        this.userId = category.userId
        this.name = category.name
        this.parentCategoryId = category.parentCategoryId
        this.color = category.color
        this.dateCreated = category.dateCreated
        this.lastUpdated = category.lastUpdated
    }
}