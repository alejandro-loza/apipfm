package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import mx.finerio.pfm.api.validation.WebHookCreateCommand
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false,
        includes = 'id, url, nature')
class Webhook  implements GormEntity<Webhook> {

    Long id
    String url
    String nature
    Date dateCreated
    Date lastUpdated
    Date dateDeleted
    Client client

    static constraints = {
        url nullable:false
        nature nullable:false
        dateDeleted nullable:true
    }

    static mapping = {
        autoTimestamp true
    }

    Webhook(){}

    Webhook(WebHookCreateCommand cmd, Client client){
        this.url = cmd.url
        this.nature = cmd.nature
        this.client = client
    }

}
