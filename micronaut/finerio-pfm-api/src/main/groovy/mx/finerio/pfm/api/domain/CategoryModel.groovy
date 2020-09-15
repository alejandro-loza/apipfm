package mx.finerio.pfm.api.domain

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false,
        includes = 'id, name, color')
class CategoryModel {

    Long id
    String name
    String color
    CategoryModel parent
    Date dateCreated
    Date lastUpdated
    Date dateDeleted

    static constraints = {
        name nullable: false, blank: false
        color nullable: true, blank: false
        dateDeleted nullable: true
    }

    static mapping = {
        autoTimestamp true
    }

    boolean isSubcategory() {
        this?.parent
    }
}