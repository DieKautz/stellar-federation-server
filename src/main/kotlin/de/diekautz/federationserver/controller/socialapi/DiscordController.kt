package de.diekautz.federationserver.controller.socialapi

import de.diekautz.federationserver.config.DiscordClientConfiguration
import de.diekautz.federationserver.config.FederationConfiguration
import de.diekautz.federationserver.controller.SessionType
import de.diekautz.federationserver.controller.socialapi.dto.DiscordTokenResponse
import de.diekautz.federationserver.controller.socialapi.dto.DiscordUser
import de.diekautz.federationserver.datasource.FederationAddressDataSource
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.MemoType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.servlet.view.RedirectView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@RestController
@RequestMapping("/api/discord")
class DiscordController (
    private val discordConfig: DiscordClientConfiguration,
    private val dataSource: FederationAddressDataSource,
    private val fedConfig: FederationConfiguration,
    @Autowired private val restTemplate: RestTemplate
    ) {

    @GetMapping("/login")
    fun redirectDiscordLogin() =
        RedirectView("https://discordapp.com/api/oauth2/authorize?client_id=${discordConfig.id}&scope=identify&response_type=code&redirect_uri=${discordConfig.callbackUrl}")

    @GetMapping("/callback")
    fun handleCallback(request: HttpServletRequest, session: HttpSession): RedirectView {
        if (request.getParameter("code") == null) {
            throw IllegalStateException("Discord Authorization failed!")
        }
        val code = request.getParameter("code")!!

        try {
            val tokenResponse = exchangeCode(code)
            session.setAttribute("type", SessionType.DISCORD)
            session.setAttribute("discordToken", tokenResponse)

            val httpHeaders = HttpHeaders()
            httpHeaders.setBearerAuth(tokenResponse.accessToken)
            val user = restTemplate.exchange<DiscordUser>(
                url = "https://discordapp.com/api/users/@me",
                method = HttpMethod.GET,
                requestEntity = HttpEntity("", httpHeaders)
            )
            session.setAttribute("discordUser", user.body!!)
        } catch (ex: Exception) {
            session.invalidate()
            throw IllegalStateException("Discord Authorization failed!")
        }
        return RedirectView("/dashboard")
    }

    @PostMapping("/my")
    fun updateOwnAddress(request: HttpServletRequest, session: HttpSession): RedirectView {
        if (session.getAttribute("discordUser") == null) {
            return RedirectView("/dashboard")
        }
        val user = session.getAttribute("discordUser") as DiscordUser
        val stellarAddress = "${user.username}#${user.discriminator}*${fedConfig.domain}"

        val accountID = request.getParameter("account_id")
        val memoType = MemoType.valueOf(request.getParameter("memo_type"))
        val memo = request.getParameter("memo")

        val federationAddress = FederationAddress(stellarAddress, accountID, memoType, memo)
        if(!dataSource.updateAddr(federationAddress)) {
            dataSource.createAddr(FederationAddress(stellarAddress, accountID, memoType, memo))
        }
        println("Updating $stellarAddress")

        return(RedirectView("/dashboard?saved"))
    }

    @PostMapping("/delete")
    fun deleteOwnAddress(session: HttpSession): ResponseEntity<String> {
        val user = session.getAttribute("discordUser") as DiscordUser
        val fedAddress = "${user.username}#${user.discriminator}*${fedConfig.domain}"
        dataSource.deleteAddr(fedAddress)
        println("Deleting $fedAddress")

        session.removeAttribute("discordUser")
        session.removeAttribute("id")
        session.removeAttribute("type")
        session.removeAttribute("discordUser")
        session.invalidate()
        return ResponseEntity.ok("deleted")
    }

    private fun exchangeCode(code: String): DiscordTokenResponse {
        val credsString = "${discordConfig.id}:${discordConfig.secret}"
        val creds = Base64.getEncoder().encodeToString(credsString.toByteArray())

        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_FORM_URLENCODED
        httpHeaders.setBasicAuth(creds)

        val data = LinkedMultiValueMap(
            mapOf(
                "client_id" to listOf(discordConfig.id),
                "client_secret" to listOf(discordConfig.secret),
                "grant_type" to listOf("authorization_code"),
                "code" to listOf(code),
                "redirect_uri" to listOf(discordConfig.callbackUrl),
            ))

        val tokenResponse = restTemplate.exchange<DiscordTokenResponse>(
            url = "https://discordapp.com/api/oauth2/token",
            method = HttpMethod.POST,
            requestEntity = HttpEntity(data, httpHeaders)
        )
        if (!tokenResponse.statusCode.is2xxSuccessful){
            throw IllegalStateException("Discord token exchange failed!")
        }
        return tokenResponse.body!!
    }
}