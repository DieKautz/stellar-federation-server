package de.diekautz.federationserver.controller.socialapi.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GitHubUser(
    @JsonProperty("login")
    val username: String,

    val id: Long,

    @JsonProperty("avatar_url")
    val avatarUrl: String,
)
