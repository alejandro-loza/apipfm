package mx.finerio.pfm.api.services.imp

import mx.finerio.pfm.api.exceptions.BadRequestException
import mx.finerio.pfm.api.services.UtilsService

import java.time.ZonedDateTime

class UtilsServiceImp implements UtilsService{

    public static final Date FROM_LIMIT = Date.from(ZonedDateTime.now().minusMonths(6).toInstant())

    @Override
    Date validateFromDate(Long dateFrom) {
        Date from = new Date(dateFrom)
        if(from.before(FROM_LIMIT)){
            throw new BadRequestException("date.range.invalid")
        }
        from
    }

    @Override
    Date validateToDate(Long dateTo, Date from) {
        Date to =  new Date(dateTo)
        if(to.before(from)){
            throw new BadRequestException("date.range.invalid")
        }
        to
    }
}
