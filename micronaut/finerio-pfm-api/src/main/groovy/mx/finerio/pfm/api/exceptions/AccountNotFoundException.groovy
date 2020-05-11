package mx.finerio.pfm.api.exceptions

class AccountNotFoundException extends NotFoundException {
    AccountNotFoundException(String message ) {
        super( message )
    }
}
