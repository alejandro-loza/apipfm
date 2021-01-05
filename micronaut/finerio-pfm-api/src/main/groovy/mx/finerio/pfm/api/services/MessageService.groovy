package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.logging.Log

interface MessageService {

    @Log
    String getMessage( String key )

}
