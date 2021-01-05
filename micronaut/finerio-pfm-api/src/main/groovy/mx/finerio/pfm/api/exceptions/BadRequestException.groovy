package mx.finerio.pfm.api.exceptions

class BadRequestException extends RuntimeException {
    BadRequestException( String message ) {
        super( message )
    }
}
