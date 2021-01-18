package mx.finerio.pfm.api.services.imp

import io.micronaut.context.MessageSource
import mx.finerio.pfm.api.services.MessageService

import javax.inject.Inject

class MessageServiceImp implements MessageService {
    @Inject
    MessageSource messageSource

    @Override
    String getMessage( String key ) throws Exception {
        messageSource.getMessage( key, MessageSource.MessageContext.DEFAULT, key)
    }
}
