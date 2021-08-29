package de.diekautz.federationserver.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
class FederationAddressEntity() {

    @Id
    var stellarAddress: String = ""
    var accountId: String = ""
    var memoType: MemoType = MemoType.NONE
    var memo: String? = ""

    fun toFederationAddress() = FederationAddress(stellarAddress, accountId, memoType, memo)

    constructor(federationAddress: FederationAddress) : this() {
        this.stellarAddress = federationAddress.stellarAddress
        this.accountId = federationAddress.accountId
        this.memoType = federationAddress.memoType
        this.memo = federationAddress.memo
    }
}