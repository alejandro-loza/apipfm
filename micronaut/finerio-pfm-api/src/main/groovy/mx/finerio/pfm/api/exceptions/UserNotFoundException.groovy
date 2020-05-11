package mx.finerio.pfm.api.exceptions

class UserNotFoundException extends NotFoundException {
    UserNotFoundException ( String message ) {
        super( message )
    }
}
