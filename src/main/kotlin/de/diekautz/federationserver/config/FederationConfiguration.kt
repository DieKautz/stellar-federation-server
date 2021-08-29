package de.diekautz.federationserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "stellar.federation")
@ConstructorBinding
data class FederationConfiguration(
    val domain: String
)
