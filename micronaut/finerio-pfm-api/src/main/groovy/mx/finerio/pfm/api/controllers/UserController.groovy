package mx.finerio.pfm.api.controllers

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.utils.SecurityService
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.logging.RequestLogger
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.dtos.resource.BudgetDto
import mx.finerio.pfm.api.dtos.resource.ResourcesDto
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.logging.Log
import mx.finerio.pfm.api.services.*
import mx.finerio.pfm.api.validation.UserCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static io.reactivex.Single.just

@Controller("/users")
@Validated
@Secured('isAuthenticated()')
class UserController {

    @Inject
    UserService userService

    @Inject
    SecurityService securityService

    @Inject
    ClientService clientService

    @Inject
    TransactionService transactionService

    @Inject
    AccountService accountService

    @Inject
    BudgetService budgetService

    @Inject
    CategoryService categoryService

    @Inject
    NextCursorService nextCursorService

    @Post("/")
    Single<UserDto> save(@Body @Valid UserCommand cmd){
        just(new UserDto(userService.create(cmd, getCurrentLoggedClient())))
    }

    @RequestLogger
    @Log
    @Get("/{id}")
    Single<UserDto> show(@NotNull Long id) {
        just(new UserDto(userService.getUser(id)))
    }

    @Log
    @Get("{?cursor}")
    Single<ResourcesDto>  showAll(@Nullable Long cursor) {
        nextCursorService.generateResourcesDto( cursor
                ? userService.getAllByClientAndCursor(getCurrentLoggedClient(), cursor)
                :  userService.getAllByClient(getCurrentLoggedClient())
        )
    }

    @Log
    @Put("/{id}")
    Single<UserDto> edit(@Body @Valid UserCommand cmd, @NotNull Long id ) {
        just(new UserDto(userService.update(cmd,id)))
    }

    @Log
    @Delete("/{id}")
    @Transactional
    HttpResponse delete(@NotNull Long id) {
        User user = userService.getUser(id)
        deleteAllUserChildEntities(user)
        userService.delete(user)
        HttpResponse.noContent()
    }

    void deleteAllUserChildEntities(User user) {
        accountService.findAllByUserBoundedByMaxRows(user).each { Account account ->
            transactionService.deleteAllByAccount(account)
            accountService.delete(account)
        }
        budgetService.findAllByUser(user).each { BudgetDto budgetDto ->
            budgetService.delete(budgetDto.id)
        }
        categoryService.findAllByUser(user).each { Category category ->
            categoryService.delete(category)
        }
    }

    private Client getCurrentLoggedClient() {
        clientService.findByUsername(securityService.getAuthentication().get().name)
    }
}
