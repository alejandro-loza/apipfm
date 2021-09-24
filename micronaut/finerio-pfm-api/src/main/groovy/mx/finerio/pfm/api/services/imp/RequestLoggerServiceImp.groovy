package mx.finerio.pfm.api.services.imp

import io.micronaut.aop.MethodInvocationContext
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.RequestLogger
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.AccountDto
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.dtos.utilities.RequestLoggerDto
import mx.finerio.pfm.api.enums.EventType
import mx.finerio.pfm.api.services.RequestLoggerService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.RequestLoggerGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService
import mx.finerio.pfm.api.validation.RequestLoggerFiltersCommand
import mx.finerio.pfm.api.validation.TransactionFiltersCommand

import javax.inject.Inject
import java.lang.reflect.Method

class RequestLoggerServiceImp extends  ServiceTemplate implements RequestLoggerService{

    @Inject
    RequestLoggerGormService requestLoggerGormService

    @Inject
    UserGormService userGormService

    @Inject
    AccountGormService accountGormService

    @Inject
    TransactionGormService transactionGormService

    @Override
    RequestLogger create(MethodInvocationContext<Object, Object> context, returnValue) {
        def functionMap  = [
               'UserServiceImp.create': userResponseType,
               'UserServiceImp.update': userResponseType,
               'UserServiceImp.getUser': userDtoResponseType,
               'UserServiceImp.delete': userDeleteResponseType,
               'AccountServiceImp.create': accountResponseType,
               'AccountServiceImp.getAccount': accountDtoResponseType,
               'AccountServiceImp.update': accountResponseType,
               'AccountServiceImp.delete': accountDeleteResponseType,
               'AccountServiceImp.findAllByUserAndCursor': accountDtoListResponseType,
               'AccountServiceImp.findAllAccountDtosByUser': accountDtoListResponseType,
               'TransactionServiceImp.create': transactionResponseType,
               'TransactionServiceImp.update': transactionResponseType,
               'TransactionServiceImp.getById': transactionDtoResponseType,
               'TransactionServiceImp.delete': transactionDeleteResponseType,
               'TransactionServiceImp.findAllByAccountAndCursor': transactionDtoListType,
               'TransactionServiceImp.findAllByAccountAndFilters': transactionDtoListType
        ]


        String eventName = getFullMethodName(context.targetMethod)
        RequestLogger request = functionMap[eventName](
                ['returnValue' :returnValue,
                 'parameters': context.parameters,
                 'eventType': eventTypeMap[eventName]]) as RequestLogger
        return requestLoggerGormService.save(request)
    }

    @Override
    String getFullMethodName(Method method ) {

        def clazz = method.declaringClass
        def className = clazz.simpleName
        def methodName = method.name
        return "${className}.${methodName}"
    }

    @Override
    List<RequestLoggerDto> findByFilters(RequestLoggerFiltersCommand args){

        def filterMap =    [
                "userId": userIdFilter,
                "eventType": eventTypeFilter,
                "dateFrom": fromDateFilter,
                "dateTo": toDateFilter]

        List<RequestLoggerDto> requestLoggers = []
        if(args.cursor){
            requestLoggers = requestLoggerGormService.findAllByIdLessThanEquals(args.cursor,
                            [max: MAX_ROWS, sort: 'id', order: 'desc'] )
                    .collect{generateRequestLoggerDto(it)}
        }
        else {
            requestLoggers = requestLoggerGormService.findAll(
                    [max: MAX_ROWS, sort: 'id', order: 'desc'] )
                    .collect{generateRequestLoggerDto(it)}
        }

        def filtersCommandProperties = generateProperties(args)

        if(filtersCommandProperties && requestLoggers){
            List<List<RequestLoggerDto>> filterLists = filtersCommandProperties.collect {
                filterMap[it.key as String](requestLoggers, args)
            }
            return filterLists ? intersectResultList(filterLists): []
        }

        requestLoggers
    }

    private static List<RequestLoggerDto> intersectResultList(List<List<RequestLoggerDto>> filterLists) {
        List<RequestLoggerDto> resultSet = filterLists?.first()
        filterLists.each {
            resultSet = resultSet.intersect(it)
        }
        resultSet
    }

    static RequestLoggerDto generateRequestLoggerDto(RequestLogger requestLogger){
        RequestLoggerDto dto = new RequestLoggerDto()
        dto.with {
            id = requestLogger.id
            userId = requestLogger.user?.id
            eventType = requestLogger.eventType
            dateCreated = requestLogger.dateCreated
        }
        dto
    }


    private Map<Object, Object> generateProperties(RequestLoggerFiltersCommand cmd) {
        cmd.properties.findAll {
            if (it.getValue() != null && it.getKey() != 'class' && it.getKey() != 'cursor') {
                return it
            }
        }
    }

    def userIdFilter = { List<RequestLoggerDto> logDtos, RequestLoggerFiltersCommand cmd ->
        logDtos.findAll {it.userId == cmd.userId }
    }

    def eventTypeFilter = { List<RequestLoggerDto> logDtos, RequestLoggerFiltersCommand cmd ->
        logDtos.findAll {it.eventType.toString() == cmd.eventType }
    }

    def fromDateFilter = {List<RequestLoggerDto> loggerDtos, RequestLoggerFiltersCommand cmd ->
        loggerDtos.findAll {it.dateCreated >= new Date(cmd.dateFrom)}
    }

    def toDateFilter = {List<RequestLoggerDto> loggerDtos, RequestLoggerFiltersCommand cmd ->
        loggerDtos.findAll {it.dateCreated <=  new Date(cmd.dateTo)}
    }

    def userResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof User) {
            RequestLogger request = new RequestLogger()
            request.with {
                user = object.returnValue as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def userDtoResponseType = { Object object ->

        if(object.returnValue != null && object.returnValue instanceof UserDto) {
            RequestLogger request = new RequestLogger()
            request.with {
                user = userGormService.findById(object.returnValue.id) as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def userDeleteResponseType = { Object object ->
        if(object.parameters.values().first()?.value instanceof User){
            RequestLogger request = new RequestLogger()
            request.with {
                user =  object.parameters.values().first().value as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def accountResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof Account) {
            RequestLogger request = new RequestLogger()
            request.with {
                user = object.returnValue.user as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def accountDtoResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof AccountDto) {
            RequestLogger request = new RequestLogger()
            request.with {
                user = accountGormService.getById(object.returnValue.id).user as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def accountDeleteResponseType = { Object object ->
        if(object.parameters.values().first()?.value instanceof Account){
            RequestLogger request = new RequestLogger()
            request.with {
                user = object.parameters.values().first().value.user as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def accountDtoListResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof List<AccountDto>) {
            RequestLogger request = new RequestLogger()
            request.with {
                user = userGormService.findById(object.parameters.userId.value) as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def transactionResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof Transaction) {
            RequestLogger request = new RequestLogger()
            request.with {
                user =  object.returnValue.account.user as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def transactionDtoResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof TransactionDto) {
            RequestLogger request = new RequestLogger()
            request.with {
                user =  transactionGormService.getById(object.returnValue.id).account.user as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def transactionDeleteResponseType = { Object object ->
        if(object.parameters.values().first()?.value instanceof Transaction){
            RequestLogger request = new RequestLogger()
            request.with {
                user = object.parameters.transaction.value.account.user as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def transactionDtoListType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof List<TransactionDto>) {
            RequestLogger request = new RequestLogger()
            request.with {
                user =  object.parameters.account.value.user as User
                request.eventType = object.eventType
            }
            return request
        }
        return
    }

    def eventTypeMap =
            [
                    'UserServiceImp.create': EventType.USER_CREATE,
                    'UserServiceImp.update': EventType.USER_UPDATE,
                    'UserServiceImp.getUser': EventType.USER_GET,
                    'UserServiceImp.delete': EventType.USER_DELETE,
                    'AccountServiceImp.create': EventType.ACCOUNT_CREATE,
                    'AccountServiceImp.getAccount': EventType.ACCOUNT_GET,
                    'AccountServiceImp.update': EventType.ACCOUNT_UPDATE,
                    'AccountServiceImp.delete': EventType.ACCOUNT_DELETE,
                    'AccountServiceImp.findAllByUserAndCursor': EventType.ACCOUNT_LIST,
                    'AccountServiceImp.findAllAccountDtosByUser': EventType.ACCOUNT_LIST,
                    'TransactionServiceImp.create': EventType.TRANSACTION_CREATE,
                    'TransactionServiceImp.update': EventType.TRANSACTION_UPDATE,
                    'TransactionServiceImp.getById': EventType.TRANSACTION_GET,
                    'TransactionServiceImp.delete': EventType.TRANSACTION_DELETE,
                    'TransactionServiceImp.findAllByAccountAndCursor': EventType.TRANSACTION_LIST,
                    'TransactionServiceImp.findAllByAccountAndFilters': EventType.TRANSACTION_LIST
            ]
}
