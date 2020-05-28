package mx.finerio.pfm.api.services.gorm

import grails.gorm.services.Query
import grails.gorm.services.Service
import mx.finerio.pfm.api.domain.Client
import mx.finerio.pfm.api.domain.ClientRole
import mx.finerio.pfm.api.domain.Role

@Service(ClientRole)
interface ClientRoleGormService {

    ClientRole save(Client client, Role role )

    ClientRole find(Client client, Role role )

    @Query("""select $r.authority
  from ${ClientRole ur}
  inner join ${Client u = ur.client}
  inner join ${Role r = ur.role}
  where $u.username = $username""")
    List<String> findAllAuthoritiesByUsername( String username )

}