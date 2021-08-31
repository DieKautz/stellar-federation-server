package de.diekautz.federationserver.controller.socialapi

import de.diekautz.federationserver.config.DiscordClientConfiguration
import de.diekautz.federationserver.config.FederationConfiguration
import de.diekautz.federationserver.controller.SessionType
import de.diekautz.federationserver.controller.socialapi.dto.DiscordTokenResponse
import de.diekautz.federationserver.controller.socialapi.dto.DiscordUser
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.MemoType
import de.diekautz.federationserver.service.FederationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@RestController
@RequestMapping("/api/discord")
class DiscordController(
    private val discordConfig: DiscordClientConfiguration,
    private val service: FederationService,
    private val fedConfig: FederationConfiguration,
    @Autowired private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException, redirAttr: RedirectAttributes): RedirectView {
        redirAttr.addFlashAttribute("error", e.message)
        return RedirectView("/dashboard")
    }

    @GetMapping("/login")
    fun redirectDiscordLogin() =
        RedirectView("https://discordapp.com/api/oauth2/authorize?client_id=${discordConfig.id}&scope=identify&response_type=code&redirect_uri=${discordConfig.callbackUrl}")

    @GetMapping("/callback")
    fun handleCallback(request: HttpServletRequest, session: HttpSession, redirAttr: RedirectAttributes): RedirectView {
        if (request.getParameter("code") == null) {
            redirAttr.addFlashAttribute("error", "Discord Authorization failed!")
            return RedirectView("/login")
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
            log.debug("Authenticated Discord user ${user.body?.username}#${user.body?.discriminator} (${user.body?.id})")
        } catch (ex: Exception) {
            session.invalidate()
            redirAttr.addFlashAttribute("error", "Discord Authorization failed!")
            return RedirectView("/login")
        }
        return RedirectView("/dashboard")
    }

    @PostMapping("/my")
    fun updateOwnAddress(request: HttpServletRequest, session: HttpSession, redirAttr: RedirectAttributes): RedirectView {
        if (session.getAttribute("discordUser") == null) {
            return RedirectView("/dashboard")
        }
        val user = session.getAttribute("discordUser") as DiscordUser
        val stellarAddress = "${user.username}#${user.discriminator}*${fedConfig.domain}"

        val accountID = request.getParameter("account_id")
        val memoType = MemoType.valueOf(request.getParameter("memo_type"))
        val memo = if(memoType!=MemoType.NONE) request.getParameter("memo").trim() else null

        val federationAddress = FederationAddress(stellarAddress, accountID, memoType, memo)

        service.updateFedAddress(federationAddress)

        log.debug("Updating discord fed address for ${user.username}#${user.discriminator} (${user.id}) to $federationAddress")

        redirAttr.addFlashAttribute("success", "Discord Federation Address saved!")
        return RedirectView("/dashboard")
    }

    @PostMapping("/delete")
    fun deleteOwnAddress(session: HttpSession): ResponseEntity<String> {
        val user = session.getAttribute("discordUser") as DiscordUser
        val fedAddress = "${user.username}#${user.discriminator}*${fedConfig.domain}"
        service.deleteFedAddress(fedAddress)

        log.debug("Deleting discord fed address for ${user.username}#${user.discriminator} (${user.id})")

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
            )
        )

        val tokenResponse = restTemplate.exchange<DiscordTokenResponse>(
            url = "https://discordapp.com/api/oauth2/token",
            method = HttpMethod.POST,
            requestEntity = HttpEntity(data, httpHeaders)
        )
        if (!tokenResponse.statusCode.is2xxSuccessful) {
            throw IllegalStateException("Discord token exchange failed!")
        }
        return tokenResponse.body!!
    }
}