package mx.finerio.pfm.api.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires

@ConfigurationProperties(CategorizerConfig.PREFIX)
@Requires(property = CategorizerConfig.PREFIX)
class CategorizerConfig {
    static final String PREFIX = "categorizer"
    static final String CATEGORIZER_API_URL = "http://ec2-3-12-253-103.us-east-2.compute.amazonaws.com:8085"

    private String username
    private String password

    String getUsername() {
        return username
    }

    void setUsername(String username) {
        this.username = username
    }

    String getPassword() {
        return password
    }

    void setPassword(String password) {
        this.password = password
    }
}
