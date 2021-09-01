package de.diekautz.federationserver.controller

import de.diekautz.federationserver.config.StellarTomlConfiguration
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.FederationError
import de.diekautz.federationserver.service.FederationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping
class FederationController(
    private val service: FederationService,
    private val tomlConfig: StellarTomlConfiguration,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException) = FederationError(e.message ?: "Error")

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [IllegalArgumentException::class, MissingServletRequestParameterException::class])
    fun handleBadRequest(e: IllegalArgumentException) = FederationError(e.message ?: "Error")

    @GetMapping("/federation")
    fun getFedAddr(@RequestParam("type") type: String, @RequestParam("q") query: String, response: HttpServletResponse): FederationAddress {
        when(type) {
            "name" -> {
                log.info("Got name-type query for '$query'")
                response.setHeader("Access-Control-Allow-Origin", "*")
                return service.getByFedAddress(query)
            }
            else -> {
                throw IllegalArgumentException("Request type '$type' not supported.")
            }
        }
    }

    @GetMapping("/.well-known/stellar.toml")
    fun serveStellarToml(response: HttpServletResponse) {
        if (tomlConfig.general.isEmpty()) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            return
        }

        val sb = StringBuilder("# Stellar Social Federation Service\n\n")
        for((key, value) in tomlConfig.general) {
            sb.append("$key=\"$value\"\n")
        }

        response.setHeader("Access-Control-Allow-Origin", "*")
        response.contentType = "text/plain"
        response.outputStream.print(sb.toString())
    }
}