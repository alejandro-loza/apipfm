package mx.finerio.pfm.api.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxStreamingHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.ErrorDto
import mx.finerio.pfm.api.dtos.ErrorsDto
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.exceptions.FinancialEntityNotFoundException
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.validation.FinancialEntityCommand
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@Property(name = 'spec.name', value = 'financial entity controller')
@MicronautTest(application = Application.class)
class FinancialEntityControllerSpec extends Specification {

    public static final String FINANCIAL_ROOT = "/financialEntities"

    @Shared
    @Inject
    @Client("/")
    RxStreamingHttpClient client

    @Inject
    FinancialEntityGormService financialGormService


    def "Should get a empty list of financial entities"(){

        given:'a client'
        HttpRequest getReq = HttpRequest.GET(FINANCIAL_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Argument.listOf(FinancialEntityDto))

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().isEmpty()
    }

    def "Should create a financial entity"(){
        given:'an financial entity request body'
        FinancialEntityCommand cmd = getWakandaTestBankCommand()

        HttpRequest request = HttpRequest.POST(FINANCIAL_ROOT, cmd)

        when:
        def rsp = client.toBlocking().exchange(request, FinancialEntityDto)

        then:
        rsp.status == HttpStatus.OK
        rsp.body().with {
            id
            name == cmd.name
            code == cmd.code
            dateCreated
            lastUpdated
        }

        when:
        FinancialEntity financialEntity = financialGormService.getById(rsp.body().id)

        then:'verify'
        !financialEntity.dateDeleted
    }

    def "Should not create a financial entity an throw bad request"() {
        given:'an financial entity empty request body'
        HttpRequest request = HttpRequest.POST(FINANCIAL_ROOT, new FinancialEntityCommand())

        when:
        client.toBlocking().exchange(request, Argument.of(FinancialEntityDto) as Argument<FinancialEntityDto>,
                Argument.of(ErrorsDto))
        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorsDto> jsonError = e.response.getBody(ErrorsDto)

        then:
        jsonError.isPresent()
        jsonError.get().errors.size() ==  4
        jsonError.get().errors.each {
            assert it instanceof ErrorDto
        }

    }

    def "Should not create a financial entity an throw bad request on malformed bodyt"() {
        given:'an financial entity empty request body'
        HttpRequest request = HttpRequest.POST(FINANCIAL_ROOT, '!"·!"')

        when:
        client.toBlocking().exchange(request, Argument.of(FinancialEntityDto) as Argument<FinancialEntityDto>,
                Argument.of(ErrorDto))
        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

        when:
        Optional<ErrorDto> jsonError = e.response.getBody(ErrorDto)

        then:
        jsonError.isPresent()
        jsonError.get().with {
           assert  code == 'request.body.invalid'
           assert  title == 'Malformed request body'
           assert  detail == 'The JSON body request you sent is invalid.'
        }
    }

    def "Should get an financial entity"(){
        given:'a saved financial entity'
        FinancialEntity financialEntity = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity)

        and:
        HttpRequest getReq = HttpRequest.GET(FINANCIAL_ROOT+"/${financialEntity.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, FinancialEntityDto)

        then:
        rspGET.status == HttpStatus.OK
        rspGET.body().with {
            assert id == financialEntity.id
            assert name == financialEntity.name
            assert code == financialEntity.code
            assert dateCreated == financialEntity.dateCreated
            assert lastUpdated == financialEntity.lastUpdated
        }

        !financialEntity.dateDeleted

    }

    def "Should not get an deleted financial entity"(){
        given:'a saved financial entity'
        FinancialEntity financialEntity = new FinancialEntity(getWakandaTestBankCommand())
        financialEntity.dateDeleted = new Date()
        financialGormService.save(financialEntity)

        and:
        HttpRequest getReq = HttpRequest.GET(FINANCIAL_ROOT+"/${financialEntity.id}")

        when:
        client.toBlocking().exchange(getReq, Argument.of(FinancialEntityDto),
                FinancialEntityNotFoundException as Argument<FinancialEntityNotFoundException>)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

        assert financialEntity.dateDeleted

    }

    def "Should not get an not found financial entity and throw 404"(){
        given:'a wrong id'
        Long id = 666
        HttpRequest getReq = HttpRequest.GET(FINANCIAL_ROOT+"/${id}")

        when:
        client.toBlocking().exchange(getReq, Argument.of(FinancialEntityDto),
                FinancialEntityNotFoundException as Argument<FinancialEntityNotFoundException>)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should not get an financial entity and throw bad request"(){
        given:'a wrong path with string as id'

        HttpRequest request = HttpRequest.GET("${FINANCIAL_ROOT}/abc")

        when:
        client.toBlocking().exchange(request)
        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should update an financial entity"(){
        given:'a saved financial entity'
        FinancialEntity financialEntity = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity)

        and:'an financial entity command to update data'
        FinancialEntityCommand cmd = new FinancialEntityCommand()
        cmd.with {
            name = 'Gringotts'
            code = 'Gringotts magic bank'
        }

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${FINANCIAL_ROOT}/${financialEntity.id}",  cmd)

        when:
        def resp = client.toBlocking().exchange(request, FinancialEntityDto)

        then:
        resp.status == HttpStatus.OK
        resp.body().with {
            assert name == cmd.name
            assert code == cmd.code
            assert lastUpdated
        }

    }

    def "Should not update an financial entity on band parameters and return Bad Request"(){
        given:'A not found entity id'
        Long id = 666

        HttpRequest request = HttpRequest.PUT("${FINANCIAL_ROOT}/${id}",  new FinancialEntityCommand())

        when:
        client.toBlocking().exchange(request,FinancialEntityDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update an financial entity on wrong band parameters and return Bad Request"(){
        given:'A not found entity id'
        Long id = 666

        HttpRequest request = HttpRequest.PUT("${FINANCIAL_ROOT}/${id}",  '!"·!"')

        when:
        client.toBlocking().exchange(request,FinancialEntityDto)

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.BAD_REQUEST

    }

    def "Should not update an financial entity and throw not found exception"(){
        given:
        FinancialEntityCommand cmd = new FinancialEntityCommand()
        cmd.with {
            name = 'Gringotts'
            code = 'Gringotts magic bank'
        }

        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.PUT("${FINANCIAL_ROOT}/${notFoundId}",  cmd)

        when:
        client.toBlocking().exchange(request, Argument.of(FinancialEntityDto) as Argument<FinancialEntityDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should get a list of entities"(){

        given:'a entities list'
        FinancialEntity financialEntity1 = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity1)

        FinancialEntity financialEntity2 = new FinancialEntity(getWakandaTestBankCommand())
        financialEntity2.dateDeleted = new Date()
        financialGormService.save(financialEntity2)

        and:
        HttpRequest getReq = HttpRequest.GET(FINANCIAL_ROOT)

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<FinancialEntityDto> financialEntityDtos = body.get("data") as List<FinancialEntityDto>
        assert !(financialEntity2.id in financialEntityDtos.id)

        assert !body.get("nextCursor")
    }

    def "Should get a list of financial entities in a cursor point"(){

        given:'a entities list'
        FinancialEntity financialEntity1 = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity1)

        FinancialEntity financialEntity2 = new FinancialEntity(getWakandaTestBankCommand())
        financialEntity2.dateDeleted = new Date()
        financialGormService.save(financialEntity2)

        FinancialEntity financialEntity3 = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity3)

        FinancialEntity financialEntity4 = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity4)

        FinancialEntity financialEntity5 = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity5)


        and:
        HttpRequest getReq = HttpRequest.GET("${FINANCIAL_ROOT}?cursor=${financialEntity4.id}")

        when:
        def rspGET = client.toBlocking().exchange(getReq, Map)

        then:
        rspGET.status == HttpStatus.OK
        Map body = rspGET.getBody(Map).get()
        List<FinancialEntityDto> financialEntityDtos = body.get("data") as List<FinancialEntityDto>
        assert financialEntityDtos.first().id == financialEntity4.id
        assert !(financialEntity2.id in financialEntityDtos.id)
        assert !(financialEntity5.id in financialEntityDtos.id)
    }

    def "Should throw not found exception on delete no found entity"(){
        given:
        def notFoundId = 666

        and:'a client'
        HttpRequest request = HttpRequest.DELETE("${FINANCIAL_ROOT}/${notFoundId}")

        when:
        client.toBlocking().exchange(request, Argument.of(FinancialEntityDto) as Argument<FinancialEntityDto>, Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND

    }

    def "Should delete an financial entity"() {
        given:'a entity'
        FinancialEntity financialEntity1 = new FinancialEntity(getWakandaTestBankCommand())
        financialGormService.save(financialEntity1)

        and:'a client request'
        HttpRequest request = HttpRequest.DELETE("${FINANCIAL_ROOT}/${financialEntity1.id}")

        when:
        def response = client.toBlocking().exchange(request, FinancialEntityDto)

        then:
        response.status == HttpStatus.NO_CONTENT

        and:
        HttpRequest.GET("${FINANCIAL_ROOT}/${financialEntity1.id}")

        when:
        client.toBlocking().exchange(request, Argument.of(FinancialEntityDto) as Argument<FinancialEntityDto>,
                Argument.of(NotFoundException))

        then:
        def  e = thrown HttpClientResponseException
        e.response.status == HttpStatus.NOT_FOUND


    }

    private static FinancialEntityCommand getWakandaTestBankCommand() {
        FinancialEntityCommand cmd = new FinancialEntityCommand()
        cmd.with {
            name = 'National Bank of Wakanda'
            code = 'WAKANDA-NB'
        }
        cmd
    }


}
