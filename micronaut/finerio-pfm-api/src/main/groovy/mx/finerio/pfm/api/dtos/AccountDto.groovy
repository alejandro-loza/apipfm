package mx.finerio.pfm.api.dtos

import mx.finerio.pfm.api.domain.Account

class AccountDto  extends ResourceDto{
    Long userId
    Long financialEntityId
    String nature
    String name
    Long number
    float balance

    AccountDto() {}

    AccountDto(Account account) {
        this.id = account.id
        this.userId = account.user.id
        this.financialEntityId = account.financialEntityId
        this.nature = account.nature
        this.name = account.name
        this.number = account.number
        this.balance = account.balance
        this.dateCreated = account.dateCreated
        this.lastUpdated = account.lastUpdated
    }

}
