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
        if (!address.stellarAddress.isValidFedAddress()) {
            throw IllegalArgumentException("Stellar address is invalid!")
        }
        if (!address.accountId.isValidPubKey()) {
            throw IllegalArgumentException("Account id is invalid!")
        }
        if (address.memoType == MemoType.NONE) {
            if (address.memo != null) {
                throw IllegalArgumentException("Memo type has to match content!")
            }
        } else {
            if (address.memo.isNullOrBlank()) {
                throw IllegalArgumentException("Memo may not be empty when type is specified!")
            }
            when(address.memoType) {
                MemoType.TEXT -> {
                    if (address.memo.encodeToByteArray().size > 28) {
                        throw IllegalArgumentException("Memo is too large for type!")
                    }
                }
                MemoType.HASH -> {
                    if(!address.memo.matches(Regex("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?\$"))){
                        throw IllegalArgumentException("Memo hash is invalid! (expected base64)")
                    }
                    val fillerCount = address.memo.count { it == '=' }
                    if ((address.memo.count() - fillerCount)*3/4 > 32) {
                        throw IllegalArgumentException("Memo hash is too big!")
                    }
                }
                MemoType.ID -> {
                    if (address.memo.toULongOrNull() == null) {
                        throw IllegalArgumentException("Memo id should be 64-bit unsigned integer.")
                    }
                }
            }
        }
    }

}