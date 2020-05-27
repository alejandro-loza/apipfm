package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.exceptions.NotFoundException
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.validation.FinancialEntityCommand

import javax.inject.Inject

class FinancialEntityServiceImp extends ServiceTemplate implements FinancialEntityService {

    public static final int MAX_ROWS = 100

    @Inject
    FinancialEntityGormService financialEntityGormService

    @Override
    FinancialEntity create(FinancialEntityCommand cmd) {
        verifyBody(cmd)
        financialEntityGormService.save(new FinancialEntity(cmd))
    }

    @Override
    FinancialEntity getById(Long id) {
        Optional.ofNullable(financialEntityGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new NotFoundException('financialEntity.exist') })
    }

    @Override
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
    List<FinancialEntity> getAll() {
        financialEntityGormService.findAllByDateDeletedIsNull([max: MAX_ROWS, sort: 'id', order: 'desc'])
    }

    @Override
    List<FinancialEntity> findAllByCursor(Long cursor) {
        financialEntityGormService.findAllByDateDeletedIsNullAndIdLessThanEquals(cursor, [max: MAX_ROWS, sort: 'id', order: 'desc'])
    }

    @Override
    void delete(Long id){
        FinancialEntity entity = getById(id)
        entity.dateDeleted = new Date()
        financialEntityGormService.save(entity)
    }

}
