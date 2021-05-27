package mx.finerio.pfm.api.dtos.resource

import groovy.transform.ToString
import mx.finerio.pfm.api.domain.Account

import java.math.RoundingMode

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
        this.number = account.cardNumber
        this.dateCreated = account.dateCreated
        this.lastUpdated = account.lastUpdated
        this.chargeable = account.chargeable
        this.balance = account.balance?.setScale(2, RoundingMode.HALF_EVEN)
    }

}
