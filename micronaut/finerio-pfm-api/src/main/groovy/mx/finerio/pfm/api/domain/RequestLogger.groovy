package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity


@Entity
@ToString(includeNames = true, includePackage = false,
        includes = 'userId, type')
class RequestLogger implements GormEntity<RequestLogger> {
    String userId
    String domain
    String type //todo enum
    Date dateCreated


}

