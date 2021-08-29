package de.diekautz.federationserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "oauth2.discord.client")
@ConstructorBinding
data class DiscordClientConfiguration (
    val id: String,
    val secret: String,
)
