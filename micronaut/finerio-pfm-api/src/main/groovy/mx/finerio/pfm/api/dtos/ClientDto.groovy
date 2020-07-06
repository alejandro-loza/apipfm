package mx.finerio.pfm.api.dtos

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class ClientDto {

    Long id
    String name
    String firstLastName
    String secondLastName
    String email
    String companyName
    String username

}
