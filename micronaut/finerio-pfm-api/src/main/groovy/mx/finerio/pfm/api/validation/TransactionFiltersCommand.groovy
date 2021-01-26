package mx.finerio.pfm.api.validation

import javax.annotation.Nullable

class TransactionFiltersCommand {
    @Nullable Long cursor
    @Nullable Long categoryId
    @Nullable Boolean charge
    @Nullable BigDecimal beginAmount
    @Nullable BigDecimal finalAmount
    @Nullable Long toDate
    @Nullable Long fromDate
}
