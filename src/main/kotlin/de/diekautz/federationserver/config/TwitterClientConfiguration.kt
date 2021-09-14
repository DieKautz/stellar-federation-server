package de.diekautz.federationserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "oauth2.twitter.client")
@ConstructorBinding
data class TwitterClientConfiguration (
    val id: String,
    val secret: String,
    val callbackUrl: String,
)
