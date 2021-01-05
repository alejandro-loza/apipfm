package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.dtos.resource.FinancialEntityDto
import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.exceptions.ItemNotFoundException
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.validation.FinancialEntityCreateCommand
import mx.finerio.pfm.api.validation.FinancialEntityUpdateCommand
import mx.finerio.pfm.api.validation.ValidationCommand

import javax.inject.Inject
import grails.gorm.transactions.Transactional

class FinancialEntityServiceImp extends ServiceTemplate implements FinancialEntityService {

    @Inject
    FinancialEntityGormService financialEntityGormService

    @Override
    FinancialEntity create(FinancialEntityCreateCommand cmd) {
        verifyBody(cmd)
        Client loggedClient = getCurrentLoggedClient()
        verifyUniqueCode(cmd, loggedClient)
        financialEntityGormService.save(new FinancialEntity(cmd, loggedClient))
    }

    @Override
    FinancialEntity getById(Long id) {
        Optional.ofNullable(financialEntityGormService
                .findByIdAndClientAndDateDeletedIsNull(id, getCurrentLoggedClient()))
                .orElseThrow({ -> new ItemNotFoundException('financialEntity.notFound') })
    }

    @Override
    @Transactional
    FinancialEntity update(FinancialEntityUpdateCommand cmd, Long id) {
        verifyBody(cmd)
        verifyUniqueCode(cmd, getCurrentLoggedClient())
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
                .findAllByClientAndDateDeletedIsNull(getCurrentLoggedClient(), [max:MAX_ROWS, sort:'id', order:'desc'])
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
    void delete(FinancialEntity entity){
        entity.dateDeleted = new Date()
        financialEntityGormService.save(entity)
    }

    private void verifyUniqueCode(ValidationCommand cmd, Client loggedClient) {
        if(findByCode(cmd, loggedClient)){
            throw new BadRequestException('financialEntity.code.nonUnique')
        }
    }

    private FinancialEntity findByCode(ValidationCommand cmd, Client loggedClient) {
        financialEntityGormService.findByCodeAndClientAndDateDeletedIsNull(String.valueOf(cmd["code"]), loggedClient)
    }

}
