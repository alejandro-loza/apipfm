package mx.finerio.pfm.api.exeptions

class AccountNotFoundException extends RuntimeException {
    AccountNotFoundException(String message ) {
        super( message )
    }
}
