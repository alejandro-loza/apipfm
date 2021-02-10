package mx.finerio.pfm.api.validation

import javax.annotation.Nullable

class TransactionFiltersCommand {
    @Nullable Long cursor
    @Nullable Long categoryId
    @Nullable Boolean charge
    @Nullable BigDecimal minAmount
    @Nullable BigDecimal maxAmount
    @Nullable Long dateTo
    @Nullable Long dateFrom
    @Nullable String description
}
