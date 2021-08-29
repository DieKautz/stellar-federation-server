package de.diekautz.federationserver.datasource.persistent

import de.diekautz.federationserver.model.FederationAddressEntity
import org.springframework.data.repository.CrudRepository

interface MySQLRepository: CrudRepository<FederationAddressEntity, String> {
}