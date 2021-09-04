package de.diekautz.federationserver.controller.socialapi.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,

    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("scope")
    val scope: String,
)