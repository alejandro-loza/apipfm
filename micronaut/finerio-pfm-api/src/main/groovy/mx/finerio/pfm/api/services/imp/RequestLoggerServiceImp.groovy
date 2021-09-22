package mx.finerio.pfm.api.services.imp

import io.micronaut.aop.MethodInvocationContext
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.RequestLogger
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.resource.AccountDto
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.dtos.resource.UserDto
import mx.finerio.pfm.api.services.RequestLoggerService
import mx.finerio.pfm.api.services.gorm.AccountGormService
import mx.finerio.pfm.api.services.gorm.RequestLoggerGormService
import mx.finerio.pfm.api.services.gorm.TransactionGormService
import mx.finerio.pfm.api.services.gorm.UserGormService

import javax.inject.Inject
import java.lang.reflect.Method

class RequestLoggerServiceImp implements RequestLoggerService{

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
               'TransactionServiceImp.deleteAllByAccount': transactionDeleteByAccountResponseType,
               'TransactionServiceImp.findAllByAccountAndCursor': transactionDtoListType,
               'TransactionServiceImp.findAllByAccountAndFilters': transactionDtoListType
        ]

        String eventName = getFullMethodName(context.targetMethod)
        RequestLogger request = new RequestLogger()

        request.with {
            user = functionMap[eventName](['returnValue' :returnValue, 'parameters': context.parameters]) as User
            request.eventType = eventName
        }
        return requestLoggerGormService.save(request)
    }

    @Override
    String getFullMethodName(Method method ) {

        def clazz = method.declaringClass
        def className = clazz.simpleName
        def methodName = method.name
        return "${className}.${methodName}"
    }

    def userResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof User) {
          return  object.returnValue
        }
        return
    }

    def userDtoResponseType = { Object object ->

        if(object.returnValue != null && object.returnValue instanceof UserDto) {
            return  userGormService.findById(object.returnValue.id)
        }
        return
    }

    def userDeleteResponseType = { Object object ->
        if(object.parameters.values().first()?.value instanceof User){
            return object.parameters.values().first().value
        }
        return
    }

    def accountResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof Account) {
            return  object.returnValue.user
        }
        return
    }

    def accountDtoResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof AccountDto) {
            return  accountGormService.getById(object.returnValue.id).user
        }
        return
    }

    def accountDeleteResponseType = { Object object ->
        if(object.parameters.values().first()?.value instanceof Account){
            return object.parameters.values().first().value.user
        }
        return
    }

    def accountDtoListResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof List<AccountDto>) {
            return userGormService.findById(object.parameters.userId.value)
        }
        return
    }

    def transactionResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof Transaction) {
            return  object.returnValue.account.user
        }
        return
    }

    def transactionDtoResponseType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof TransactionDto) {
            return  transactionGormService.getById(object.returnValue.id).account.user
        }
        return
    }

    def transactionDeleteResponseType = { Object object ->
        if(object.parameters.values().first()?.value instanceof Transaction){
            return object.parameters.transaction.value.account.user
        }
        return
    }

    def transactionDeleteByAccountResponseType = { Object object ->
        if(object.parameters.values().first()?.value instanceof Account){
            return object.parameters.account.value.user
        }
        return
    }

    def transactionDtoListType = { Object object ->
        if(object.returnValue != null && object.returnValue instanceof List<TransactionDto>) {
           return object.parameters.account.value.user
        }
        return
    }
}
