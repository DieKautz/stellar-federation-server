package de.diekautz.federationserver.datasource.persistent

import de.diekautz.federationserver.datasource.FederationAddressDataSource
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.FederationAddressEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Primary
@Repository
class MySQLFedAddrDataSource(
    @Autowired val sqlRepository: MySQLRepository
): FederationAddressDataSource {
    override fun getAddresses(): Collection<FederationAddress> {
        return sqlRepository.findAll().map {
            it.toFederationAddress()
        }
    }

    override fun findAddress(stellarAddress: String): FederationAddress? {
        val elem = sqlRepository.findById(stellarAddress)
        return if(elem.isPresent) {
            elem.get().toFederationAddress()
        } else null
    }

    override fun createAddr(address: FederationAddress): FederationAddress {
        val saved = sqlRepository.save(FederationAddressEntity(address))
        return saved.toFederationAddress()
    }

    override fun updateAddr(address: FederationAddress): Boolean {
        sqlRepository.save(FederationAddressEntity(address))
        return true
    }

    override fun deleteAddr(stellarAddress: String): Boolean {
        sqlRepository.deleteById(stellarAddress)
        return true
    }


}