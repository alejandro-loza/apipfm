package mx.finerio.pfm.api.config.imp

import io.micronaut.context.annotation.ConfigurationProperties
import mx.finerio.pfm.api.config.ApplicationConfiguration

@ConfigurationProperties("application")
class ApplicationConfigurationProperties implements ApplicationConfiguration {

    protected final Integer DEFAULT_MAX = 10

    private Integer max = DEFAULT_MAX

    @Override
    Integer getMax() { max }

    void setMax(Integer max) {
        if(max) this.max = max
    }
}