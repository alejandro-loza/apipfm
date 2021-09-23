package mx.finerio.pfm.api.validation

import javax.annotation.Nullable

class RequestLoggerFiltersCommand {
    @Nullable Long cursor
    @Nullable Long userId
    @Nullable Long dateTo
    @Nullable Long dateFrom
    @Nullable String eventType
}
