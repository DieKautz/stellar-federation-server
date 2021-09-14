package de.diekautz.federationserver

import de.diekautz.federationserver.config.*
import de.diekautz.federationserver.controller.filter.CorsFilter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableConfigurationProperties(value = [
    GitHubClientConfiguration::class,
    DiscordClientConfiguration::class,
    TwitterClientConfiguration::class,
    FederationConfiguration::class,
    StellarTomlConfiguration::class])
class FederationServerApplication {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.build()

    @Bean
    fun registerCorsFilter(): FilterRegistrationBean<CorsFilter> {
        val registrationBean = FilterRegistrationBean<CorsFilter>()

        println("registering filter")
        registrationBean.filter = CorsFilter()
        registrationBean.addUrlPatterns("/federation/*")
        return registrationBean
    }
}

fun main(args: Array<String>) {
    runApplication<FederationServerApplication>(*args)
}
