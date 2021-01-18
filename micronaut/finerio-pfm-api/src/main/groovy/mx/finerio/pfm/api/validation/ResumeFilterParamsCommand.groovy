package mx.finerio.pfm.api.validation

class ResumeFilterParamsCommand {
    Long accountId
    Long dateFrom
    Long dateTo

    ResumeFilterParamsCommand(Long accountId, Long dateFrom, Long dateTo) {
        this.accountId = accountId
        this.dateFrom = dateFrom
        this.dateTo = dateTo
    }
}
