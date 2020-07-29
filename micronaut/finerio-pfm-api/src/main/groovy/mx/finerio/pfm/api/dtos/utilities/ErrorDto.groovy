package mx.finerio.pfm.api.dtos.utilities

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class ErrorDto {
    String code
    String title
    String detail
}
