package mx.finerio.pfm.api.validation

class MovementAnalysisFilterParamsCommand {
    Long dateFrom
    Long dateTo

    MovementAnalysisFilterParamsCommand(Long dateFrom, Long dateTo) {
        this.dateFrom = dateFrom
        this.dateTo = dateTo
    }
}
