package mx.finerio.pfm.api.repository.imp

import groovy.transform.CompileStatic
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession
import io.micronaut.spring.tx.annotation.Transactional
import mx.finerio.pfm.api.config.ApplicationConfiguration
import mx.finerio.pfm.api.domain.User
import mx.finerio.pfm.api.pogos.UserCreateCommand
import mx.finerio.pfm.api.repository.UserRepository

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.validation.constraints.NotBlank
import javax.inject.Singleton

@Singleton
@CompileStatic
class UserRepositoryImp  implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager
    private final ApplicationConfiguration applicationConfiguration

    UserRepositoryImp (@CurrentSession EntityManager entityManager,
                      ApplicationConfiguration applicationConfiguration) {
        this.entityManager = entityManager
        this.applicationConfiguration = applicationConfiguration
    }

    @Override
    @Transactional
    User save(@NotBlank UserCreateCommand cmd) {
        User user = new User(cmd)
        entityManager.persist(user)
        user
    }
}
