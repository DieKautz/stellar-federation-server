package de.diekautz.federationserver.controller.socialapi.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TwitterTokenRequestResponse(
    @JsonProperty("oauth_token")
    val oauthToken: String,

    @JsonProperty("oauth_token_secret")
    val oauthTokenSecret: String,

    @JsonProperty("oauth_callback_confirmed")
    val oauthCallbackConfirmed: Boolean,
)
