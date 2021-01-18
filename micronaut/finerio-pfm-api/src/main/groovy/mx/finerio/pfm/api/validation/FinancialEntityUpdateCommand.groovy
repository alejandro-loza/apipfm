package mx.finerio.pfm.api.validation

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class FinancialEntityUpdateCommand extends ValidationCommand {
    String name
    String code
}
