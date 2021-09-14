package de.diekautz.federationserver.datasource

import de.diekautz.federationserver.model.FederationAddress

interface FederationAddressDataSource {

    fun getAddresses(): Collection<FederationAddress>

    fun findAddress(stellarAddress: String): FederationAddress?

    fun findByAccId(accountId: String): FederationAddress?

    fun createAddr(address: FederationAddress): FederationAddress

    fun updateAddr(address: FederationAddress): Boolean

    fun deleteAddr(stellarAddress: String): Boolean

}