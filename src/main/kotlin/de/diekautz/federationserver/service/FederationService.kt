package de.diekautz.federationserver.service

import de.diekautz.federationserver.datasource.FederationAddressDataSource
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.MemoType
import de.diekautz.federationserver.model.isValidFedAddress
import de.diekautz.federationserver.model.isValidPubKey
import org.springframework.stereotype.Service

@Service
class FederationService(private val dataSource: FederationAddressDataSource) {

    fun getAddresses(): Collection<FederationAddress> = dataSource.getAddresses()

    fun getByFedAddress(stellarAddress: String): FederationAddress {
        if (!stellarAddress.isValidFedAddress()) {
            throw IllegalArgumentException("Address is invalid!")
        }
        return dataSource.findAddress(stellarAddress) ?: throw NoSuchElementException("Address was not found!")
    }

    fun createFedAddress(address: FederationAddress): FederationAddress {
        validateAddress(address)

        return dataSource.createAddr(address)
    }

    fun updateFedAddress(address: FederationAddress): FederationAddress {
        validateAddress(address)

        val found = dataSource.updateAddr(address)
        if (found) {
            return address
        } else {
            throw NoSuchElementException("Address was not found!")
        }
    }

    fun deleteFedAddress(stellarAddress: String) {
        val deleted = dataSource.deleteAddr(stellarAddress)
        if (!deleted) {
            throw NoSuchElementException("Address was not found!")
        }
    }


    private fun validateAddress(address: FederationAddress) {
        if (!address.stellarAddress.isValidFedAddress() || !address.accountId.isValidPubKey()) {
            throw IllegalArgumentException("Address or account id invalid!")
        }
        if (address.memoType == MemoType.NONE) {
            if (address.memo == null) {
                throw IllegalArgumentException("Memo Type has to match content!")
            } else if (address.memoType == MemoType.HASH && !address.memo.matches(Regex("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?\$"))) {
                throw IllegalArgumentException("Memo hash is invalid!")
            }
        } else {
            if (address.memo != null) {
                throw IllegalArgumentException("Memo Type has to match content!")
            }
        }
    }

}