package mx.finerio.pfm.api.services

import mx.finerio.pfm.api.domain.FinancialEntity
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
                'callbackService.create.createCallbackDto.null'
    }

}
