package mx.finerio.pfm.api.dtos

import io.micronaut.http.hateoas.Link

class UsersDto {

    List<UserDto> users
    Link nextPage

    UsersDto(List<UserDto> users, String uri) {
        this.users = users
        this.nextPage = Link.of(uri + "?offset=${users?.last()?.id}")
    }

}
