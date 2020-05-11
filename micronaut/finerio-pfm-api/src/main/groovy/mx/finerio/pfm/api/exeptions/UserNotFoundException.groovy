package mx.finerio.pfm.api.exeptions

class UserNotFoundException extends RuntimeException {
    UserNotFoundException ( String message ) {
        super( message )
    }
}
