package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.validation.ValidationCommand

class ServiceTemplate {

    public static final int MAX_ROWS = 100

    static void verifyBody(ValidationCommand cmd) {
        if (!cmd) {
            throw new IllegalArgumentException(
                    'request.body.invalid')
        }
    }

}
