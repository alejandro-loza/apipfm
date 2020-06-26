package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.FinancialEntityDto
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.validation.FinancialEntityCreateCommand
import mx.finerio.pfm.api.validation.FinancialEntityUpdateCommand

import javax.inject.Inject
import grails.gorm.transactions.Transactional

class FinancialEntityServiceImp extends ServiceTemplate implements FinancialEntityService {

    public static final int MAX_ROWS = 100

    @Inject
    FinancialEntityGormService financialEntityGormService

    @Override
    FinancialEntity create(FinancialEntityCreateCommand cmd) {
        verifyBody(cmd)
        def loggedClient = getCurrentLoggedClient()
        findByCode(cmd, loggedClient) ?: financialEntityGormService.save(new FinancialEntity(cmd, loggedClient))
    }

    private FinancialEntity findByCode(FinancialEntityCreateCommand cmd, Client loggedClient) {
        financialEntityGormService.findByCodeAndClientAndDateDeletedIsNull(cmd.code, loggedClient)
    }

    @Override
    FinancialEntity getById(Long id) {
        Optional.ofNullable(financialEntityGormService
                .findByIdAndClientAndDateDeletedIsNull(id, getCurrentLoggedClient()))
                .orElseThrow({ -> new ItemNotFoundException('financialEntity.exist') })
    }

    @Override
    @Transactional
    FinancialEntity update(FinancialEntityUpdateCommand cmd, Long id) {
        verifyBody(cmd)
        FinancialEntity financialEntity = getById(id)
        financialEntity.with {
            name = cmd.name ?: financialEntity.name
            code = cmd.code ?: financialEntity.code
            client = financialEntity.client
        }
        financialEntityGormService.save(financialEntity)
    }

    @Override
    List<FinancialEntityDto> getAll() {
        financialEntityGormService
                .findAllByClientAndDateDeletedIsNull(getCurrentLoggedClient(),[max:MAX_ROWS, sort:'id', order:'desc'])
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
