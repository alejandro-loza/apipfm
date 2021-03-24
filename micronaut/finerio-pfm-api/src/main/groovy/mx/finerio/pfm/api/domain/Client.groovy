package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import io.micronaut.security.authentication.providers.UserState
import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false,
    includes = [ 'username' ])
class Client implements GormEntity<Client>, UserState {

    Long id
    String username
    String passwordd
    boolean enabled = true
    boolean accountExpired = false
    boolean accountLocked = false
    boolean passwordExpired = false
    Date dateCreated
    Date lastUpdated
    Date dateDeleted
	
	String getPassword() {
		passwordd
	}

    static constraints = {
        id generator: 'native', params:[sequence:'CLIENT_SEQ']
        username nullable: false, blank: false, unique: true
        passwordd nullable: false, blank: false, password: true
        dateDeleted nullable:true
    }

    static mapping = {
        table 'client'
    }

}
