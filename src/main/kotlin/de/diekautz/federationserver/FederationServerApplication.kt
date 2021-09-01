package de.diekautz.federationserver

import de.diekautz.federationserver.config.DiscordClientConfiguration
import de.diekautz.federationserver.config.FederationConfiguration
import de.diekautz.federationserver.config.StellarTomlConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableConfigurationProperties(value = [DiscordClientConfiguration::class, FederationConfiguration::class, StellarTomlConfiguration::class])
class FederationServerApplication {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.build()
}

fun main(args: Array<String>) {
    runApplication<FederationServerApplication>(*args)
}
