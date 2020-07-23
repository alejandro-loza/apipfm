package mx.finerio.pfm.api.domain

import grails.gorm.annotation.Entity

import groovy.transform.ToString

import mx.finerio.pfm.api.validation.BudgetCreateCommand

import org.grails.datastore.gorm.GormEntity

@Entity
@ToString(includeNames = true, includePackage = false,
    includes = 'id, name, amount')
class Budget implements GormEntity<Budget> {

    Long id
    User user
    Category category
    String name
    BigDecimal amount
    Long parentBudgetId
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    Budget(){}

    Budget(BudgetCreateCommand cmd, User user, Category category){
        this.user = user
        this.category = category
        this.name = cmd.name
        this.amount = cmd.amount
        this.parentBudgetId = cmd.parentBudgetId
    }

    static constraints = {
        name nullable: false, blank:false
        dateDeleted nullable:true
    }

    static mapping = {
        autoTimestamp true
    }

}
