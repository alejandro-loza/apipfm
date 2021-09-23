package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import mx.finerio.pfm.api.enums.EventType
import org.grails.datastore.gorm.GormEntity


@Entity
@ToString(includeNames = true, includePackage = false,
        includes = 'user, eventType')
class RequestLogger implements GormEntity<RequestLogger> {

    Long id
    User user
    EventType eventType
    Date dateCreated

    static constraints = {
        id generator: 'native', params:[sequence:'REQUEST_LOGGER_SEQ']
        user nullable: false
        eventType nullable: false, blank:false
    }

    static mapping = {
        autoTimestamp true
    }

}

