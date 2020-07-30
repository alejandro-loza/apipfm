package mx.finerio.pfm.api.dtos.resource

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class ClientDto extends ResourceDto{

    String name
    String firstLastName
    String secondLastName
    String email
    String companyName
    String username

}
