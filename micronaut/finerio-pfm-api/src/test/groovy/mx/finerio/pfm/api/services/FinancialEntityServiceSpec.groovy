package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.imp.FinancialEntityServiceImp
import mx.finerio.pfm.api.validation.FinancialEntityCommand
import spock.lang.Specification

class FinancialEntityServiceSpec extends Specification {

    FinancialEntityService financialEntityService = new FinancialEntityServiceImp()

    void setup(){
        financialEntityService.financialEntityGormService = Mock(FinancialEntityGormService)
    }

    def 'Should save an financial entity'(){
        given:'an financial entity command request body'
        FinancialEntityCommand cmd = new FinancialEntityCommand()
        cmd.with {
            name = 'National Bank of Wakanda'
            code = 'WAKANDA-NB'
        }

        when:
        1 * financialEntityService.financialEntityGormService.save(_  as FinancialEntity) >> new FinancialEntity()

        def response = financialEntityService.create(cmd)

        then:
        response instanceof FinancialEntity

    }

    def "Should throw exception on null body"() {

        when:
        def response = financialEntityService.create(null)
        then:
        IllegalArgumentException e = thrown()
        e.message ==
                'request.body.invalid'
    }

    def "Should get an financial entity"(){

        when:
        1 * financialEntityService.financialEntityGormService.findByIdAndDateDeletedIsNull(_ as Long) >> new FinancialEntity()

        def result = financialEntityService.getById(1L)

        then:
        result instanceof FinancialEntity
    }

    def "Should not get an financial entity and throw exception"(){

        when:
        1 * financialEntityService.financialEntityGormService.findByIdAndDateDeletedIsNull(_ as Long) >> null
        financialEntityService.getById(666)

        then:
        NotFoundException e = thrown()
        e.message == 'financialEntity.exist'
    }

    def "Should get all financial entities " () {
        when:
        1 * financialEntityService.financialEntityGormService.findAllByDateDeletedIsNull(_ as Map) >> [new FinancialEntity()]
        def response = financialEntityService.getAll()

        then:
        response instanceof  List<FinancialEntity>
    }

    def "Should not get all financial entities " () {
        when:
        1 * financialEntityService.financialEntityGormService.findAllByDateDeletedIsNull(_ as Map) >> []
        def response = financialEntityService.getAll()

        then:
        response instanceof  List<FinancialEntity>
        response.isEmpty()
    }

    def "Should get  financial entities by a cursor " () {
        when:
        1 * financialEntityService.financialEntityGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(_ as Long, _ as Map) >> [new FinancialEntity()]
        def response = financialEntityService.findAllByCursor(2)

        then:
        response instanceof  List<FinancialEntity>
    }


}
