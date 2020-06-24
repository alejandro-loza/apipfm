package mx.finerio.pfm.api.exceptions

class ItemNotFoundException extends RuntimeException {
    ItemNotFoundException(String message ) {
        super( message )
    }
}
