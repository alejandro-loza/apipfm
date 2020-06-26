package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.dtos.CategoryDto
import mx.finerio.pfm.api.dtos.ResourcesDto
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.validation.CategoryCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller("/categories")
@Validated
@Secured('isAuthenticated()')
class CategoryController {

    @Inject
    CategoryService categoryService

    @Post("/")
    Single<CategoryDto> save(@Body @Valid CategoryCommand cmd){
        Single.just(new CategoryDto(categoryService.create(cmd)))
    }

    @Get("/{id}")
    @Transactional
    Single<CategoryDto> show(@NotNull Long id) {
        Single.just(new CategoryDto(categoryService.find(id)))
    }

    @Get("{?cursor}")
    @Transactional
    Single<Map> showAll(@Nullable Long cursor) {
        List<CategoryDto> categoryDtos = cursor ? categoryService.findAllByCursor(cursor) : categoryService.getAll()
        Single.just(categoryDtos.isEmpty() ? [] :  new ResourcesDto(categoryDtos)) as Single<Map>
    }

    @Put("/{id}")
    @Transactional
    Single<CategoryDto> edit(@Body @Valid CategoryCommand cmd, @NotNull Long id ) {
        Single.just(new CategoryDto(categoryService.update(cmd, id)))
    }

    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        categoryService.delete(id)
        HttpResponse.noContent()
    }

}