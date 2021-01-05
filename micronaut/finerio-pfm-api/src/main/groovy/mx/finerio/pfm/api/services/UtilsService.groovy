package mx.finerio.pfm.api.services

interface UtilsService {
    Date validateFromDate(Long dateFrom)
    Date validateToDate(Long dateTo, Date from)
}