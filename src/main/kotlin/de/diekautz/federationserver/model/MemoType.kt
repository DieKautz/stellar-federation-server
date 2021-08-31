package de.diekautz.federationserver.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class MemoType(val displayValue: String) {

    @JsonProperty("none")
    NONE("None"),

    @JsonProperty("text")
    TEXT("Text"),

    @JsonProperty("hash")
    HASH("Hash"),

    @JsonProperty("id")
    ID("Id"),
}