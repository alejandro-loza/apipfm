package mx.finerio.pfm.api.config

import javax.validation.constraints.NotNull

interface ApplicationConfiguration {

    @NotNull Integer getMax()
}