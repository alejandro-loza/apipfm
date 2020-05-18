package mx.finerio.pfm.api.exceptions

class NotFoundException extends RuntimeException {
    NotFoundException(String message ) {
        super( message )
    }
}
