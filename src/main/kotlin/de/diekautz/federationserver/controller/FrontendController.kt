package de.diekautz.federationserver.controller

import de.diekautz.federationserver.config.FederationConfiguration
import de.diekautz.federationserver.controller.SessionType.DISCORD
import de.diekautz.federationserver.controller.SessionType.NONE
import de.diekautz.federationserver.controller.socialapi.dto.DiscordUser
import de.diekautz.federationserver.datasource.FederationAddressDataSource
import de.diekautz.federationserver.model.FederationAddress
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Controller
class FrontendController(
    private val dataSource: FederationAddressDataSource,
    private val fedConfig: FederationConfiguration
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(value = [IllegalArgumentException::class, IllegalStateException::class])
    fun handleError(e: IllegalArgumentException, model: Model): String {
        model["error"] = "Error: ${e.localizedMessage}"
        return "login"
    }

    @GetMapping("/")
    fun serveHomePage(request: HttpServletRequest, session: HttpSession): RedirectView {

        return if (session.getAttribute("id") == null)
            RedirectView("/dashboard")
        else RedirectView("/login")

    }

    @GetMapping("/login")
    fun serveLoginPage(model: Model, session: HttpSession, request: HttpServletRequest, redirAttr: RedirectAttributes): String {
        if (session.getAttribute("id") == null) {
            session.setAttribute("id", UUID.randomUUID().toString())
            session.setAttribute("type", NONE)
        } else if (session.getAttribute("type") as SessionType != NONE) {
            return "redirect:/dashboard"
        }
        if (redirAttr.containsAttribute("error")) {
            model["error"] = redirAttr.getAttribute("error")!!
        }
        if (redirAttr.containsAttribute("success")) {
            model["success"] = redirAttr.getAttribute("success")!!
        }
        return "login"
    }

    @GetMapping("/logout")
    fun logoutSession(model: Model, session: HttpSession): String {
        log.debug("Logout requested for session: ${session.id}")
        session.invalidate()
        model["success"] = "Log out successful!"
        return "login"
    }

    @GetMapping("/dashboard")
    fun serveDashboard(model: Model, session: HttpSession, request: HttpServletRequest, redirAttr: RedirectAttributes): String {
        if (session.getAttribute("id") == null || session.getAttribute("type") as SessionType == NONE) {
            return "redirect:/login"
        }
        model["sessionId"] = session.getAttribute("id")
        model["type"] = session.getAttribute("type").toString()
        val discordUser = session.getAttribute("discordUser") as DiscordUser
        val username = "${discordUser.username}#${discordUser.discriminator}"
        model["socialName"] = username

        when (session.getAttribute("type") as SessionType) {
            DISCORD -> {
                model["endpoint"] = "/api/discord"
            }
            else -> {}
        }
        var fedAddress = dataSource.findAddress("${username}*${fedConfig.domain}")
        if (fedAddress == null) {
            fedAddress = FederationAddress("${username}*${fedConfig.domain}", "")
        }
        model["fedAddress"] = fedAddress

        if (redirAttr.containsAttribute("error")) {
            model["error"] = redirAttr.getAttribute("error")!!
        }
        if (redirAttr.containsAttribute("success")) {
            model["success"] = redirAttr.getAttribute("success")!!
        }
        return "dashboard"
    }
}