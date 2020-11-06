package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.security.utils.SecurityService
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.services.imp.FinancialEntityServiceImp
import mx.finerio.pfm.api.validation.FinancialEntityCreateCommand
import mx.finerio.pfm.api.validation.FinancialEntityUpdateCommand
import spock.lang.Specification

import java.security.Principal

import static java.util.Optional.of

@Property(name = 'spec.name', value = 'financial entity service')
@MicronautTest(application = Application.class)
class FinancialEntityServiceSpec extends Specification {

    FinancialEntityService financialEntityService = new FinancialEntityServiceImp()

    void setup(){
        financialEntityService.financialEntityGormService = Mock(FinancialEntityGormService)
        financialEntityService.clientService = Mock(ClientService)
        financialEntityService.securityService = Mock(SecurityService)
    }

    def 'Should save an financial entity'(){
        given:'an financial entity command request body'
        FinancialEntityCreateCommand cmd = new FinancialEntityCreateCommand()
        cmd.with {
            name = 'National Bank of Wakanda'
            code = 'WAKANDA-NB'
        }
        def client = new Client()
        def entity = new FinancialEntity(cmd,client)


        when:
        1 * financialEntityService.securityService.getAuthentication() >> of(Principal)
        1 * financialEntityService.clientService.findByUsername(_ as String) >> client
        1 * financialEntityService.financialEntityGormService
                .findByCodeAndClientAndDateDeletedIsNull(_ as String, _ as Client) >> null
        1 * financialEntityService.financialEntityGormService.save(_  as FinancialEntity) >> entity

        def response = financialEntityService.create(cmd)

        then:
        assert response instanceof FinancialEntity
        assert response.id == entity.id

    }

    def 'Should not save an financial entity on previously saves and unique code'(){
        given:'an financial entity command request body'
        FinancialEntityCreateCommand cmd = new FinancialEntityCreateCommand()
        cmd.with {
            name = 'National Bank of Wakanda'
            code = 'WAKANDA-NB'
        }
        def client = new Client()
        def entity = new FinancialEntity(cmd,client)


        when:
        1 * financialEntityService.securityService.getAuthentication() >> of(Principal)
        1 * financialEntityService.clientService.findByUsername(_ as String) >> client
        1 * financialEntityService.financialEntityGormService
                .findByCodeAndClientAndDateDeletedIsNull(_ as String, _ as Client) >> entity
        0 * financialEntityService.financialEntityGormService.save(_  as FinancialEntity)

        financialEntityService.create(cmd)

        then:
        BadRequestException e = thrown()
        e.message == 'financialEntity.code.nonUnique'
    }

    def 'Should not save an financial entity on previously saved and unique code on update'(){
        given:'an financial entity command request body'
        FinancialEntityUpdateCommand cmd = new FinancialEntityUpdateCommand()
        cmd.with {
            name = 'National Bank of Wakanda'
            code = 'WAKANDA-NB'
        }
        def client = new Client()
        FinancialEntityCreateCommand fcmd = new FinancialEntityCreateCommand()
        fcmd.with {cmd}
        def entity = new FinancialEntity(fcmd ,client)

        when:
        1 * financialEntityService.securityService.getAuthentication() >> of(Principal)
        1 * financialEntityService.clientService.findByUsername(_ as String) >> client
        1 * financialEntityService.financialEntityGormService
                .findByCodeAndClientAndDateDeletedIsNull(_ as String, _ as Client) >> entity
        0 * financialEntityService.financialEntityGormService.save(_  as FinancialEntity)

        financialEntityService.update(cmd, 1L)

        then:
        BadRequestException e = thrown()
        e.message == 'financialEntity.code.nonUnique'
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
        1 * financialEntityService.securityService.getAuthentication() >> of(Principal)
        1 * financialEntityService.clientService.findByUsername(_ as String) >>  new Client()
        1 * financialEntityService.financialEntityGormService
                .findByIdAndClientAndDateDeletedIsNull(_ as Long, _ as Client) >> new FinancialEntity()

        def result = financialEntityService.getById(1L)

        then:
        result instanceof FinancialEntity
    }

    def "Should not get an financial entity and throw exception"(){

        when:
        1 * financialEntityService.securityService.getAuthentication() >> of(Principal)
        1 * financialEntityService.clientService.findByUsername(_ as String) >>  new Client()
        1 * financialEntityService.financialEntityGormService
                .findByIdAndClientAndDateDeletedIsNull(_ as Long, _ as Client) >> null
        financialEntityService.getById(666)

        then:
        ItemNotFoundException e = thrown()
        e.message == 'financialEntity.notFound'
    }

    def "Should get all financial entities " () {
        when:
        1 * financialEntityService.securityService.getAuthentication() >> of(Principal)
        1 * financialEntityService.clientService.findByUsername(_ as String) >>  new Client()
        1 * financialEntityService.financialEntityGormService
                .findAllByClientAndDateDeletedIsNull(_ as Client ,_ as Map) >> [new FinancialEntity()]
        def response = financialEntityService.getAll()

        then:
        response instanceof  List<FinancialEntity>
    }

    def "Should not get all financial entities " () {
        when:
        1 * financialEntityService.securityService.getAuthentication() >> of(Principal)
        1 * financialEntityService.clientService.findByUsername(_ as String) >>  new Client()
        1 * financialEntityService.financialEntityGormService
                .findAllByClientAndDateDeletedIsNull(_ as Client,_ as Map) >> []
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
