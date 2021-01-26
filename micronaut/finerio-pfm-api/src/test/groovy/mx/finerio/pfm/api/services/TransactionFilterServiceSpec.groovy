package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.dtos.resource.TransactionDto
import mx.finerio.pfm.api.services.imp.TransactionFilterServiceImp
import mx.finerio.pfm.api.validation.TransactionFiltersCommand
import spock.lang.Specification

import io.micronaut.test.annotation.MicronautTest
import spock.lang.Unroll

import java.time.ZonedDateTime

@Property(name = 'spec.name', value = 'transaction service')
@MicronautTest(application = Application.class)
class TransactionFilterServiceSpec extends Specification {
    TransactionFilterService transactionFilterService = new TransactionFilterServiceImp()

    @Unroll
    def "should filter by command charge #cmd.charge and find #length records " (TransactionFiltersCommand cmd, int length){
        given:
        TransactionDto transactionDto1 = new TransactionDto()
        transactionDto1.with {
            date = new Date()
            charge = true
            amount = 100.00
            categoryId = 1
        }

        TransactionDto transactionDto2 = new TransactionDto()
        transactionDto2.with {
            date = new Date()
            charge = true
            amount = 5000.00
            categoryId = 1
        }

        TransactionDto transactionDto3 = new TransactionDto()
        transactionDto3.with {
            date = new Date()
            charge = false
            amount = 100.00
            categoryId = 1
        }

        List<TransactionDto> dtos =[transactionDto1, transactionDto2, transactionDto3]

        expect:
        transactionFilterService.filterTransactions(dtos, cmd).size() == length

        where:
                               cmd        | length
        generateChargeCommand(true)       | 2
        generateChargeCommand(false)      | 1

    }

    def "should filter by categoryId" (){
        given:
        TransactionDto transactionDto1 = new TransactionDto()
        transactionDto1.with {
            date = new Date()
            charge = true
            amount = 100.00
            categoryId = 1
        }

        TransactionDto transactionDto2 = new TransactionDto()
        transactionDto2.with {
            date = new Date()
            charge = true
            amount = 5000.00
            categoryId = 2
        }

        TransactionDto transactionDto3 = new TransactionDto()
        transactionDto3.with {
            date = new Date()
            charge = false
            amount = 100.00
            categoryId = 1
        }

        List<TransactionDto> dtos =[transactionDto1, transactionDto2, transactionDto3]

        and:'a command to filter by category id'
        TransactionFiltersCommand cmd = new TransactionFiltersCommand()
        cmd.with {
            categoryId = 2
        }

        when:
        def response = transactionFilterService.filterTransactions(dtos, cmd)

        then:
        assert response.size() == 1
        assert response.first() == transactionDto2
    }

    def "should filter by begin date" (){
        given:

        Date sixMonths = Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fourMonths = Date.from(ZonedDateTime.now().minusMonths(4).toInstant())
        Date twoMonths = Date.from(ZonedDateTime.now().minusMonths(2).toInstant())

        TransactionDto transactionDto1 = new TransactionDto()
        transactionDto1.with {
            date = sixMonths
            charge = true
            amount = 100.00
            categoryId = 1
        }

        TransactionDto transactionDto2 = new TransactionDto()
        transactionDto2.with {
            date = fourMonths
            charge = true
            amount = 5000.00
            categoryId = 2
        }

        TransactionDto transactionDto3 = new TransactionDto()
        transactionDto3.with {
            date = twoMonths
            charge = false
            amount = 100.00
            categoryId = 1
        }

        List<TransactionDto> dtos =[transactionDto1, transactionDto2, transactionDto3]


        and:'a command to filter by category id'
        TransactionFiltersCommand cmd = new TransactionFiltersCommand()
        cmd.with {
            fromDate = fourMonths.getTime()
        }

        when:
        def response = transactionFilterService.filterTransactions(dtos, cmd)

        then:
        assert response.size() == 2
        assert response.every {it.date >= fourMonths
        }
        assert !response.contains(transactionDto1)
        assert response.contains(transactionDto2)
        assert response.contains(transactionDto3)

    }

    def "should filter by final date" (){
        given:

        Date sixMonths = Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
        Date fourMonths = Date.from(ZonedDateTime.now().minusMonths(4).toInstant())
        Date twoMonths = Date.from(ZonedDateTime.now().minusMonths(2).toInstant())

        TransactionDto transactionDto1 = new TransactionDto()
        transactionDto1.with {
            date = sixMonths
            charge = true
            amount = 100.00
            categoryId = 1
        }

        TransactionDto transactionDto2 = new TransactionDto()
        transactionDto2.with {
            date = fourMonths
            charge = true
            amount = 5000.00
            categoryId = 2
        }

        TransactionDto transactionDto3 = new TransactionDto()
        transactionDto3.with {
            date = twoMonths
            charge = false
            amount = 100.00
            categoryId = 1
        }

        List<TransactionDto> dtos =[transactionDto1, transactionDto2, transactionDto3]

        and:'a command to filter by category id'
        TransactionFiltersCommand cmd = new TransactionFiltersCommand()
        cmd.with {
            toDate = fourMonths.getTime()
        }

        when:
        def response = transactionFilterService.filterTransactions(dtos, cmd)

        then:
        assert response.size() == 2
        assert response.every {it.date <= fourMonths
        }
        assert response.contains(transactionDto1)
        assert response.contains(transactionDto2)
        assert !response.contains(transactionDto3)

    }

    def "should filter by begin amount" (){
        given:

        TransactionDto transactionDto1 = new TransactionDto()
        transactionDto1.with {
            date = new Date()
            charge = true
            amount = 500.00
            categoryId = 1
        }

        TransactionDto transactionDto2 = new TransactionDto()
        transactionDto2.with {
            date = new Date()
            charge = true
            amount = 5000.00
            categoryId = 2
        }

        TransactionDto transactionDto3 = new TransactionDto()
        transactionDto3.with {
            date = new Date()
            charge = false
            amount = 499.00
            categoryId = 1
        }

        List<TransactionDto> dtos =[transactionDto1, transactionDto2, transactionDto3]


        and:'a command to filter by category id'
        TransactionFiltersCommand cmd = new TransactionFiltersCommand()
        cmd.with {
            beginAmount = 500.00
        }

        when:
        def response = transactionFilterService.filterTransactions(dtos, cmd)

        then:
        assert response.size() == 2
        assert response.every {it.amount >= 500.00
        }
        assert response.contains(transactionDto1)
        assert response.contains(transactionDto2)
        assert !response.contains(transactionDto3)

    }

    def "should filter by final amount" (){
        given:

        TransactionDto transactionDto1 = new TransactionDto()
        transactionDto1.with {
            date = new Date()
            charge = true
            amount = 500.00
            categoryId = 1
        }

        TransactionDto transactionDto2 = new TransactionDto()
        transactionDto2.with {
            date = new Date()
            charge = true
            amount = 5000.00
            categoryId = 2
        }

        TransactionDto transactionDto3 = new TransactionDto()
        transactionDto3.with {
            date = new Date()
            charge = false
            amount = 499.00
            categoryId = 1
        }

        List<TransactionDto> dtos =[transactionDto1, transactionDto2, transactionDto3]


        and:'a command to filter by category id'
        TransactionFiltersCommand cmd = new TransactionFiltersCommand()
        cmd.with {
            finalAmount = 500.00
        }

        when:
        def response = transactionFilterService.filterTransactions(dtos, cmd)

        then:
        assert response.size() == 2
        assert response.every {it.amount <= 500.00
        }
        assert response.contains(transactionDto1)
        assert !response.contains(transactionDto2)
        assert response.contains(transactionDto3)

    }

    def "should filter by multiple params" (){
       given:
       Date sixMonths = Date.from(ZonedDateTime.now().minusMonths(6).toInstant())
       Date fourMonths = Date.from(ZonedDateTime.now().minusMonths(4).toInstant())
       Date twoMonths = Date.from(ZonedDateTime.now().minusMonths(2).toInstant())

       TransactionDto transactionDto1 = new TransactionDto()
       transactionDto1.with {
           date = sixMonths
           charge = true
           amount = 100.00
           categoryId = 1
       }

       TransactionDto transactionDto2 = new TransactionDto()
       transactionDto2.with {
           date = fourMonths
           charge = true
           amount = 200.00
           categoryId = 1
       }

       TransactionDto transactionDto3 = new TransactionDto()
       transactionDto3.with {
           date = twoMonths
           charge = false
           amount = 300.00
           categoryId = 1
       }

       TransactionDto transactionDto4 = new TransactionDto()
       transactionDto4.with {
           date = fourMonths
           charge = true
           amount = 400.00
           categoryId = 1
       }

       TransactionDto transactionDto5 = new TransactionDto()
       transactionDto5.with {
           date = twoMonths
           charge = false
           amount = 600.00
           categoryId = 1
       }

       TransactionDto transactionDto6 = new TransactionDto()
       transactionDto6.with {
           date = new Date()
           charge = false
           amount = 500.00
           categoryId = 1
       }

       TransactionDto transactionDto7 = new TransactionDto()
       transactionDto7.with {
           date = fourMonths
           charge = true
           amount = 400.00
           categoryId = 2
       }


       List<TransactionDto> dtos =[transactionDto1,
                                   transactionDto2,
                                   transactionDto3,
                                   transactionDto4,
                                   transactionDto5,
                                   transactionDto6,
                                   transactionDto7]

        and:'a command to filter'
        TransactionFiltersCommand cmd = new TransactionFiltersCommand()
        cmd.with {
            fromDate = fourMonths.getTime()
            toDate  = twoMonths.getTime()
            beginAmount = 250.00
            finalAmount = 500.00
            charge = true
            categoryId = 2
        }

        when:
        def response = transactionFilterService.filterTransactions(dtos, cmd)

        then:
        assert !response.contains(transactionDto1)
        assert !response.contains(transactionDto6)
        assert !response.contains(transactionDto2)
        assert !response.contains(transactionDto3)
        assert !response.contains(transactionDto4)
        assert !response.contains(transactionDto5)
        assert response.contains(transactionDto7)

    }

    TransactionFiltersCommand generateChargeCommand(Boolean chargeToSet){
        TransactionFiltersCommand cmd = new TransactionFiltersCommand()
        cmd.with {
            charge = chargeToSet
        }
        cmd
    }

}
