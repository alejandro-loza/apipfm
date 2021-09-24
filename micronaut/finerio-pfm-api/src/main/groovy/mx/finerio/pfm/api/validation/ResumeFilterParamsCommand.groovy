package mx.finerio.pfm.api.validation

class ResumeFilterParamsCommand {
    Long userId
    Long dateFrom
    Long dateTo
    String eventType

    ResumeFilterParamsCommand(Long userId, Long dateFrom, Long dateTo, String eventType) {
        this.userId = userId
        this.dateFrom = dateFrom
        this.dateTo = dateTo
        this.eventType = eventType
    }
}
