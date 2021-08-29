package de.diekautz.federationserver.controller.socialapi.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DiscordTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("expires_in")
    val expiresIn: Int,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("scope")
    val scope: String,
)
