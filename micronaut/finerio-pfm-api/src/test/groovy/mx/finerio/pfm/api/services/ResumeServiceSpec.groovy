package mx.finerio.pfm.api.services

import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MicronautTest
import mx.finerio.pfm.api.Application
import mx.finerio.pfm.api.domain.Account
import mx.finerio.pfm.api.domain.Category
import mx.finerio.pfm.api.domain.Transaction
import mx.finerio.pfm.api.dtos.utilities.BalancesDto
import mx.finerio.pfm.api.dtos.utilities.MovementsDto
import spock.lang.Specification
import javax.inject.Inject
import java.text.SimpleDateFormat

@Property(name = 'spec.name', value = 'resume service')
@MicronautTest(application = Application.class)
class ResumeServiceSpec extends Specification {

    @Inject
    ResumeService resumeService

    def "Should Group by month"() {
        given:
        Category parent1 = new Category()
        parent1.with {
            name = 'transporte'
            color = 'red'
            id = 1
        }

        Category sub1 = new Category()
        sub1.with {
            name = 'uber'
            color = 'red'
            parent = parent1
            id = 11
        }

        Category sub2 = new Category()
        sub2.with {
            name = 'viajes'
            color = 'red'
            parent = parent1
            id = 12
        }

        Category parent2 = new Category()
        parent2.with {
            name = 'entretenimiento'
            color = 'red'
            id = 2
        }

        Category sub3 = new Category()
        sub3.with {
            name = 'gaming'
            color = 'red'
            parent = parent2
            id = 23
        }

        Category sub4 = new Category()
        sub4.with {
            name = 'conciertos'
            color = 'red'
            parent = parent2
            id = 24
        }


        Date september98Date = new SimpleDateFormat("dd/MM/yyyy").parse("13/09/1998")
        Date december20Date = new SimpleDateFormat("dd/MM/yyyy").parse("20/12/2020")
        Date december19Date = new SimpleDateFormat("dd/MM/yyyy").parse("20/12/2019")

        def t2 = generateTransaction(december20Date, sub2)
        def t3 = generateTransaction(december20Date, sub3)

        def t4 = generateTransaction(september98Date, sub4)
        def t5 = generateTransaction(september98Date, sub4)
        def t1 = generateTransaction(september98Date, sub1)
        List<Transaction> transactions = [t1, t2, t3, t4, t5]

        when:
        List<MovementsDto> movementsByMonth = resumeService.groupTransactionsByMonth(transactions)

        then:
        assert movementsByMonth.size() == 2
        assert movementsByMonth.last().amount == 300
        def date = new  Date(movementsByMonth.first().date)
        assert date.month == december20Date.month
        assert date.year == december20Date.year
        assert movementsByMonth.last().categories.size() == 2
        assert movementsByMonth.last().categories.first().amount == 100
        assert movementsByMonth.last().categories.last().amount == 200

        assert movementsByMonth.last().categories.first().categoryId == 1
        assert movementsByMonth.last().categories.last().categoryId == 2

        assert movementsByMonth.first().amount == 200
        def date2 = new  Date(movementsByMonth.last().date)
        assert date2.month == september98Date.month
        assert date2.year == september98Date.year
        assert movementsByMonth.first().categories.size() == 2

        assert movementsByMonth.first().categories.first().categoryId == 1
        assert movementsByMonth.first().categories.last().categoryId == 2
        assert movementsByMonth.first().categories.first().amount == 100
        assert movementsByMonth.first().categories.last().amount == 100

    }

    def "Should get the balance"(){
        given:

        Date december98Date = new SimpleDateFormat("dd/MM/yyyy").parse("13/12/1998")
        Date december20Date = new SimpleDateFormat("dd/MM/yyyy").parse("20/12/2020")
        Date december19Date = new SimpleDateFormat("dd/MM/yyyy").parse("20/12/20")

        MovementsDto incomeMovements98 = new MovementsDto()
        incomeMovements98.with {
            date = december98Date.getTime()
            amount = 235.35
        }

        MovementsDto incomeMovements20 = new MovementsDto()
        incomeMovements20.with {
            date = december20Date.getTime()
            amount = 300.00
        }

        MovementsDto expensesMovements98 = new MovementsDto()
        expensesMovements98.with {
            date = december98Date.getTime()
            amount = 100.00
        }

        MovementsDto expensesMovements20 = new MovementsDto()
        expensesMovements20.with {
            date = december20Date.getTime()
            amount = 99.99
        }

        MovementsDto expensesMovements19 = new MovementsDto()
        expensesMovements19.with {
            date = december19Date.getTime()
            amount = 99.99
        }

        when:
        List<BalancesDto> result =resumeService
                .getBalance([incomeMovements98, incomeMovements20],  [expensesMovements98, expensesMovements20, expensesMovements19])

        def balanceDates = result.collect{
            new Date(it.date)
        }

        then:
        assert result instanceof List<BalancesDto>
        assert !result.isEmpty()
        assert balanceDates.size() == 3
        assert  balanceDates.find{it.date == december20Date.date}
        assert  balanceDates.find{it.date == december19Date.date}
        assert  balanceDates.find{it.date == december98Date.date}

        def result98 = result.find{it.date == december98Date.getTime()}
        result98.with {
            assert incomes == 235.35F
            assert expenses == 100.00F
        }

        def result19 = result.find{it.date == december19Date.getTime()}
        result19.with {
           assert incomes == 0.0F
           assert expenses == 99.99F
        }

        def result20 = result.find{it.date == december20Date.getTime()}
        result20.with {
            assert incomes == 300.0F
            assert expenses == 99.99F
        }

    }

    private static Transaction generateTransaction(Date date1, Category category1) {
        Transaction transaction = new Transaction()
        transaction.with {
            account = new Account()
            charge = true
            description = 'rapi'
            amount = 100.00
            date = date1
            category = category1
        }
        transaction
    }
}
