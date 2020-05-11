package mx.finerio.pfm.api.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import io.micronaut.context.MessageSource
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated
import io.reactivex.Single
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.dtos.AccountDto
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.ResourcesResponseDto
import mx.finerio.pfm.api.exceptions.AccountNotFoundException
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.UserService
import mx.finerio.pfm.api.services.gorm.AccountService
import mx.finerio.pfm.api.services.gorm.UserServiceRepository
import mx.finerio.pfm.api.validation.AccountCommand

import javax.annotation.Nullable
import javax.inject.Inject
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.constraints.NotNull

@Controller("/accounts")
@Validated
class AccountController {

    public static final int MAX_ROWS = 100

    @Inject
    AccountService accountService

    @Inject
    UserService userService

    @Inject
    MessageSource messageSource

    @Post("/")
    Single<AccountDto> save(@Body @Valid AccountCommand cmd){
        def user = userService.getUser(cmd.userId)
        Single.just(new AccountDto(accountService.save(new Account(cmd, user))))
    }

    @Get("/{id}")
    Single<AccountDto> show(@NotNull Long id) {
        Single.just(new AccountDto(getAccount(id)))
    }

    @Get("{?cursor}")
    Single<Map> showAll(@Nullable Long cursor) {
        List<AccountDto> accounts = cursor ? findAllByCursor(cursor) : findAll()
        Single.just(accounts.isEmpty() ? [] :  new ResourcesResponseDto(accounts)) as Single<Map>
    }

    @Put("/{id}")
    Single<AccountDto> edit(@Body @Valid AccountCommand cmd, @NotNull Long id ) {
        Account account = getAccount(id)
        account.with {
            user = cmd.user
            financialEntityId = cmd.financialEntityId
            nature = cmd.nature
            name = cmd.name
            number = cmd.number
            balance = cmd.balance
        }
        Single.just(new AccountDto(accountService.save(account)))
    }

    @Delete("/{id}")
    HttpResponse delete(@NotNull Long id) {
        Account account = getAccount(id)
        account.dateDeleted = new Date()
        accountService.save(account)
        HttpResponse.noContent()
    }

    @Error
    HttpResponse<List<ErrorDto>> jsonError(ConstraintViolationException constraintViolationException) {
        HttpResponse.<List<ErrorDto>> status(HttpStatus.BAD_REQUEST,
                messageBuilder("request.body.invalid.title").get()).body(
                constraintViolationException.constraintViolations.collect {
                    new ErrorDto(it.message, messageSource)
                })
    }

    @Error(status = HttpStatus.BAD_REQUEST)
    HttpResponse<ErrorDto> badRequest(HttpRequest request) {
        HttpResponse.<ErrorDto>badRequest().body(
                new ErrorDto('request.body.invalid', this.messageSource)
        )
    }

    @Error(exception = NotFoundException)
    HttpResponse notFound(NotFoundException ex) {
        HttpResponse.notFound().body(ex.message)
    }

    @Error(exception = JsonProcessingException)
    HttpResponse<ErrorDto> badRequest(JsonProcessingException ex) {
        badRequestResponse()
    }

    @Error(exception = ConversionErrorException)
    HttpResponse<ErrorDto> badRequest(ConversionErrorException ex) {
        badRequestResponse()
    }

    private MutableHttpResponse<ErrorDto> badRequestResponse() {
        HttpResponse.<ErrorDto> badRequest().body(new ErrorDto('request.body.invalid', this.messageSource))
    }

    private Optional<String> messageBuilder(String code) {
        messageSource.getMessage(code, MessageSource.MessageContext.DEFAULT)
    }

    private Account getAccount(long id) {
        Optional.ofNullable(accountService.getById(id))
                .orElseThrow({ -> new AccountNotFoundException('The account ID you requested was not found.') })
    }

    private List<AccountDto> findAll() {
        accountService.findAll([max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new AccountDto(it)}
    }

    private List<AccountDto> findAllByCursor(long cursor) {
        accountService.findByIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc']).collect{new AccountDto(it)}
    }

}