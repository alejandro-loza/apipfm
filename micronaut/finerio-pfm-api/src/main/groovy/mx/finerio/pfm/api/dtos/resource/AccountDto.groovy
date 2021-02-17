package mx.finerio.pfm.api.dtos.resource

import groovy.transform.ToString
import mx.finerio.pfm.api.domain.Account

@ToString(includeNames = true, includePackage = false,
    includeSuperProperties = true)
class AccountDto  extends ResourceDto{
    String nature
    String name
    String number
    BigDecimal balance
    boolean chargeable

    AccountDto() {}

    AccountDto(Account account) {
        this.id = account.id
        this.nature = account.nature
        this.name = account.name
        this.number = account.number
        this.dateCreated = account.dateCreated
        this.lastUpdated = account.lastUpdated
        this.chargeable = account.chargeable
        this.balance =  new BigDecimal(account.balance).setScale(2,BigDecimal.ROUND_HALF_UP)
    }

}
