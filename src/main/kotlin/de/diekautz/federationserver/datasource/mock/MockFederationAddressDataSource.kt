package de.diekautz.federationserver.datasource.mock

import de.diekautz.federationserver.datasource.FederationAddressDataSource
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.MemoType
import org.springframework.stereotype.Repository

@Repository
class MockFederationAddressDataSource : FederationAddressDataSource {

    val accs = mutableListOf(
        FederationAddress("alice*local.test", "ABC123"),
        FederationAddress("bob*local.test", "AGE31B1223", MemoType.TEXT, "Lorem"),
        FederationAddress("claire*remote.dev", "B1423", MemoType.HASH, "TG9yZW0="),
    )

    override fun getAddresses(): Collection<FederationAddress> {
        return accs
    }

    override fun findAddress(stellarAddress: String): FederationAddress? {
        return accs.firstOrNull { it.stellarAddress == stellarAddress }
    }

    override fun findByAccId(accountId: String): FederationAddress? {
        return accs.firstOrNull { it.accountId == accountId }
    }

    override fun createAddr(address: FederationAddress): FederationAddress {
        accs.add(address)
        return address
    }

    override fun updateAddr(address: FederationAddress): Boolean {
        val found = accs.removeIf { it.stellarAddress == address.stellarAddress }
        if (found) {
            accs.add(address)
        }
        return found
    }

    override fun deleteAddr(stellarAddress: String): Boolean {
        return accs.removeIf { it.stellarAddress == stellarAddress }
    }
}