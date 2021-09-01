package de.diekautz.federationserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "stellar.toml")
@ConstructorBinding
data class StellarTomlConfiguration(
    val general: Map<String, String> = mapOf()
)
