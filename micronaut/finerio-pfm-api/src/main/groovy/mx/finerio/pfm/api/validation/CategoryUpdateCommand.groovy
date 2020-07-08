package mx.finerio.pfm.api.validation

class CategoryUpdateCommand extends ValidationCommand {
    Long userId
    String name
    String color
    Long parentCategoryId
}
