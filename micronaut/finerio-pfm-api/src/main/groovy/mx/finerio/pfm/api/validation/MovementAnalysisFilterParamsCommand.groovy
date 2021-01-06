package mx.finerio.pfm.api.validation

class MovementAnalysisFilterParamsCommand {
    Long userId
    Long dateFrom
    Long dateTo

    MovementAnalysisFilterParamsCommand(Long userId, Long dateFrom, Long dateTo) {
        this.userId = userId
        this.dateFrom = dateFrom
        this.dateTo = dateTo
    }
}
