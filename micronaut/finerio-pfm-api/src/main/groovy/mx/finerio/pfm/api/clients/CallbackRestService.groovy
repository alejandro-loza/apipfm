package mx.finerio.pfm.api.clients

import mx.finerio.pfm.api.logging.Log

interface CallbackRestService {

    @Log
    Integer post( String url, Object body ) throws Exception

}