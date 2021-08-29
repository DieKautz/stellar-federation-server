package de.diekautz.federationserver.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.util.Base64Utils

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FederationAddress(

    @JsonProperty("stellar_address")
    val stellarAddress: String,

    @JsonProperty("account_id")
    val accountId: String,

    @JsonProperty("memo_type")
    val memoType: MemoType = MemoType.NONE,

    val memo: String? = null,
) {

}

fun String.isValidFedAddress(): Boolean {
    val split = this.split("*")
    if (split.size != 2) {
        return false
    }
    val username = split[0]
    val domain = split[1]
    if (!Charsets.US_ASCII.newEncoder().canEncode(username) || username.contains(Regex("[*>]+"))) {
        return false
    }
    if (!domain.matches(Regex("(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]"))) {
        return false
    }
    return true
}

fun String.isValidPubKey(): Boolean {
    //TODO: check for valid bytes in key (ED25519 curve)
    return Base64Utils.decodeFromString(this).size == 42
}
