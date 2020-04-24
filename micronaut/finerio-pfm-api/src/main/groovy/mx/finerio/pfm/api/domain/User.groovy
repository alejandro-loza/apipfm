package mx.finerio.pfm.api.domain

import mx.finerio.pfm.api.pogos.UserCreateCommand

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull

@Entity
@Table(name = "user")
class User {

    User(UserCreateCommand userCmd) {
        this.username = userCmd.username
        this.firstName = userCmd.firstName
        this.lastName = userCmd.lastName
        this.email = userCmd.email
        this.phone = userCmd.phone
        this.userStatus = userCmd.userStatus
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id

    @NotNull
    @Column(name = "username", nullable = false)
    private String username

    @NotNull
    @Column(name = "firstName", nullable = false)
    private String firstName

    @NotNull
    @Column(name = "lastName", nullable = false)
    private String lastName

    @NotNull
    @Email
    @Column(name = "email", nullable = false)
    private String email

    @NotNull
    @Column(name = "phone", nullable = false)
    private Long phone

    @NotNull
    @Column(name = "userStatus", nullable = false)
    private int userStatus


}
