package mx.finerio.pfm.api.clients.imp

import com.fasterxml.jackson.databind.ObjectMapper
import mx.finerio.pfm.api.clients.CallbackRestService

import javax.inject.Singleton

@Singleton
class CallbackRestServiceImpl implements CallbackRestService {

    @Override
    Integer post( String url, Object body ) throws Exception {

        def post = new URL( url ).openConnection()
        post.requestMethod = 'POST'
        post.doOutput = true
        post.setRequestProperty( 'Content-Type', 'application/json' )

        def objectMapper = new ObjectMapper()
        def message = objectMapper.writeValueAsString( body )
        post.outputStream.write( message.getBytes( 'UTF-8' ) )
        return post.responseCode

    }

}