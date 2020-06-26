package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.validation.FinancialEntityCommand

import javax.inject.Inject
import grails.gorm.transactions.Transactional

class FinancialEntityServiceImp extends ServiceTemplate implements FinancialEntityService {

    public static final int MAX_ROWS = 100

    @Inject
    FinancialEntityGormService financialEntityGormService

    @Override
    FinancialEntity create(FinancialEntityCommand cmd) {
        verifyBody(cmd)
        financialEntityGormService.save(new FinancialEntity(cmd, getCurrentLoggedClient()))
    }

    @Override
    FinancialEntity getById(Long id) {
        Optional.ofNullable(financialEntityGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new ItemNotFoundException('financialEntity.exist') })
    }

    @Override
    @Transactional
    FinancialEntity update(FinancialEntityCommand cmd, Long id) {
        verifyBody(cmd)
        FinancialEntity financialEntity = getById(id)
        financialEntity.with {
            name = cmd.name
            code = cmd.code
        }
        financialEntityGormService.save(financialEntity)
    }

    @Override
    List<FinancialEntityDto> getAll() {
        financialEntityGormService
                .findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new FinancialEntityDto(it)}
    }

    @Override
    List<FinancialEntityDto> findAllByCursor(Long cursor) {
        financialEntityGormService
                .findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
                .collect{new FinancialEntityDto(it)}
    }

    @Override
    @Transactional
    void delete(Long id){
        FinancialEntity entity = getById(id)
        entity.dateDeleted = new Date()
        financialEntityGormService.save(entity)
    }

}
