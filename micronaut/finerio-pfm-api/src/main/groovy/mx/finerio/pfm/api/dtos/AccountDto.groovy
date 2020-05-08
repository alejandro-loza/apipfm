package mx.finerio.pfm.api.dtos

import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.User

class AccountDto {
    Long id
    User user
    Long financialEntityId
    String nature
    String name
    Long number
    float balance
    Date dateCreated
    Date lastUpdated

    AccountDto() {}

    AccountDto(Account account) {
        this.id = account.id
        this.user = account.user
        this.financialEntityId = account.financialEntityId
        this.nature = account.nature
        this.name = account.name
        this.number = account.number
        this.balance = account.balance
        this.dateCreated = account.dateCreated
        this.lastUpdated = account.lastUpdated
    }

}
