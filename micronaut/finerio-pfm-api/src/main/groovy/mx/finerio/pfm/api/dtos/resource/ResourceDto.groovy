package mx.finerio.pfm.api.dtos.resource

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
abstract class ResourceDto {
   abstract Long id
   abstract Date dateCreated
   abstract Date lastUpdated
}
