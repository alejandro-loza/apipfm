package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.config.CategorizerConfig
import mx.finerio.pfm.api.dtos.resource.CategoryDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.CategoryService
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.validation.CategoryCreateCommand
import mx.finerio.pfm.api.validation.CategoryUpdateCommand

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

    @Inject
    UserService userService

    @Inject
    @Client(CategorizerConfig.CATEGORIZER_API_URL)
    RxStreamingHttpClient client

    @Value('${categorizer.username}')
    String username

    @Value('${categorizer.password}')
    String password

    @Log
    @Post("/")
    Single<CategoryDto> save(@Body @Valid CategoryCreateCommand cmd){
        Single.just(new CategoryDto(categoryService.create(cmd)))
    }

    @Log
    @Get("/{id}")
    @Transactional
    Single<CategoryDto> show(@NotNull Long id) {
        Single.just(new CategoryDto(categoryService.getById(id)))
    }

    @Log
    @Get("{?userId}")
    @Transactional
    Single<ResourcesDto> showAll( @Nullable Long userId) {
        List<CategoryDto> clientCategories
            clientCategories = categoryService.findAllByCurrentLoggedClientAndUserNul()
        if(userId) {
            clientCategories.addAll( categoryService.findAllByUser(userService.getUser(userId)))
        }

        Single.just(new ResourcesDto(clientCategories, null))
    }

    @Log
    @Put("/{id}")
    @Transactional
    Single<CategoryDto> edit(@Body @Valid CategoryUpdateCommand cmd, @NotNull Long id ) {
        Single.just(new CategoryDto(categoryService.update(cmd, id)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        categoryService.delete(id)
        HttpResponse.noContent()
    }

    @Log
    @Get("/search")
    @Transactional
    HttpResponse categorize(@QueryValue('input') String input) {

        HttpRequest getReq = HttpRequest.GET("/search?input=${input}")
                    .basicAuth(username, password)
        def result = client.toBlocking().exchange(getReq, Map)
        System.out.println(result)
        return HttpResponse.ok().body(result)
    }

}
