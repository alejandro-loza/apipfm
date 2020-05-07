package mx.finerio.pfm.api.validation

import mx.finerio.pfm.api.domain.User

class AccountCommand {
    User user
    Long financialEntityId
    String nature
    String name
    Long number
    float balance
}
