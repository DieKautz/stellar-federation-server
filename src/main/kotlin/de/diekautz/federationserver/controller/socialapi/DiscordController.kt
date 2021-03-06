package de.diekautz.federationserver.controller.socialapi

import de.diekautz.federationserver.config.DiscordClientConfiguration
import de.diekautz.federationserver.config.FederationConfiguration
import de.diekautz.federationserver.controller.SessionType
import de.diekautz.federationserver.controller.socialapi.dto.DiscordTokenResponse
import de.diekautz.federationserver.controller.socialapi.dto.DiscordUser
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.MemoType
import de.diekautz.federationserver.model.UserSession
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

    @ExceptionHandler(Exception::class)
    fun handleOtherEx(e: Exception): RedirectView {
        log.error("An unhandled error occurred! ${e.message}\n${e.stackTraceToString()}")
        return RedirectView("/")
    }

    @GetMapping("/login")
    fun redirectDiscordLogin() =
        RedirectView("https://discordapp.com/api/oauth2/authorize?client_id=${discordConfig.id}&scope=identify&response_type=code&redirect_uri=${discordConfig.callbackUrl}")

    @GetMapping("/callback")
    fun handleCallback(request: HttpServletRequest, session: HttpSession, redirAttr: RedirectAttributes): RedirectView {
        val userSession = session.getAttribute("user") as UserSession? ?: return RedirectView("/login")
        if (request.getParameter("code") == null) {
            redirAttr.addFlashAttribute("error", "Discord Authorization failed!")
            return RedirectView("/login")
        }
        val code = request.getParameter("code")!!

        try {
            val tokenResponse = exchangeCode(code)
            userSession.sessionType = SessionType.DISCORD

            val httpHeaders = HttpHeaders()
            httpHeaders.setBearerAuth(tokenResponse.accessToken)
            val user = restTemplate.exchange<DiscordUser>(
                url = "https://discordapp.com/api/users/@me",
                method = HttpMethod.GET,
                requestEntity = HttpEntity("", httpHeaders)
            )
            userSession.username = "${user.body!!.username}#${user.body!!.discriminator}"
            log.debug("Authenticated Discord user ${userSession.username} (id:${user.body?.id}) SESSION:${userSession.id}")
        } catch (ex: Exception) {
            session.setAttribute("user", null)
            session.invalidate()
            redirAttr.addFlashAttribute("error", "Discord Authorization failed!")
            return RedirectView("/login")
        }
        return RedirectView("/dashboard")
    }

    @PostMapping("/my")
    fun updateOwnAddress(request: HttpServletRequest, session: HttpSession, redirAttr: RedirectAttributes): RedirectView {
        val userSession = session.getAttribute("user") as UserSession? ?: return RedirectView("/dashboard")

        val stellarAddress = "${userSession.username}*${userSession.sessionType.subDomain}.${fedConfig.domain}"

        val accountID = request.getParameter("account_id")
        val memoType = MemoType.valueOf(request.getParameter("memo_type"))
        val memo = if(memoType!=MemoType.NONE) request.getParameter("memo").trim() else null

        val federationAddress = FederationAddress(stellarAddress, accountID, memoType, memo)

        service.updateFedAddress(federationAddress)

        log.debug("Updating discord fed address for ${userSession.username} to $federationAddress")

        redirAttr.addFlashAttribute("success", "Discord Federation Address saved!")
        return RedirectView("/dashboard")
    }

    @PostMapping("/delete")
    fun deleteOwnAddress(session: HttpSession): ResponseEntity<String> {
        val userSession = session.getAttribute("user") as UserSession? ?: throw IllegalArgumentException("Unauthorized delete request.")

        val stellarAddress = "${userSession.username}*${userSession.sessionType.subDomain}.${fedConfig.domain}"
        service.deleteFedAddress(stellarAddress)

        log.debug("Deleting discord fed address for ${userSession.username}")

        session.setAttribute("user", null)
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