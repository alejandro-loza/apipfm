package mx.finerio.pfm.api.dtos

import io.micronaut.context.MessageSource

class UserErrorDto {

    String code
    String title
    String detail
    private MessageSource messageSource

    UserErrorDto(String code, MessageSource messageSource) {
        this.messageSource = messageSource
        this.code = code
        this.title = getPropertyMessage(code)
        this.detail = getPropertyMessage(code.concat('.detail'))
    }


    private String getPropertyMessage(String code) {
        messageSource.getMessage(getFixedPropertyName(code), MessageSource.MessageContext.DEFAULT).orElse(null)
    }

    private static String getFixedPropertyName(String property) {
        property.split(':').last().replaceAll("\\s", "")
    }

}
