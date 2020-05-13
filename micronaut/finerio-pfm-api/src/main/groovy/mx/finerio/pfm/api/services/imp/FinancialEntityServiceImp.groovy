package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.domain.FinancialEntity
import mx.finerio.pfm.api.exceptions.FinancialEntityNotFoundException
import mx.finerio.pfm.api.services.FinancialEntityService
import mx.finerio.pfm.api.services.gorm.FinancialEntityGormService
import mx.finerio.pfm.api.validation.FinancialEntityCommand

import javax.inject.Inject

class FinancialEntityServiceImp implements FinancialEntityService {

    public static final int MAX_ROWS = 100

    @Inject
    FinancialEntityGormService financialEntityGormService

    @Override
    FinancialEntity create(FinancialEntityCommand cmd) {
        if ( !cmd  ) {
            throw new IllegalArgumentException(
                    'callbackService.create.createCallbackDto.null' )
        }
        financialEntityGormService.save(new FinancialEntity(cmd))
    }

    @Override
    FinancialEntity getById(Long id) {
        Optional.ofNullable(financialEntityGormService.findByIdAndDateDeletedIsNull(id))
                .orElseThrow({ -> new FinancialEntityNotFoundException() })
    }
}
