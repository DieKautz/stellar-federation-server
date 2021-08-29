package de.diekautz.federationserver.controller

import de.diekautz.federationserver.config.FederationConfiguration
import de.diekautz.federationserver.controller.SessionType.*
import de.diekautz.federationserver.controller.socialapi.dto.DiscordUser
import de.diekautz.federationserver.datasource.FederationAddressDataSource
import de.diekautz.federationserver.model.FederationAddress
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.view.RedirectView
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Controller
class FrontendController(
    private val dataSource: FederationAddressDataSource,
    private val fedConfig: FederationConfiguration
) {

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
    fun serveLoginPage(model: Model, session: HttpSession): String {
        if (session.getAttribute("id") == null) {
            session.setAttribute("id", UUID.randomUUID().toString())
            session.setAttribute("type", NONE)
        } else if (session.getAttribute("type") as SessionType != NONE) {
            return "redirect:/dashboard"
        }
        return "login"
    }

    @GetMapping("/logout")
    fun logoutSession(model: Model, session: HttpSession): String {
        session.invalidate()
        model["success"] = "Log out successful!"
        return "redirect:/login"
    }

    @GetMapping("/dashboard")
    fun serveDashboard(model: Model, session: HttpSession, request: HttpServletRequest): String {
        if (session.getAttribute("id") == null || session.getAttribute("type") as SessionType == NONE) {
            println("redirecting back to login ${session.getAttribute("id")} #### ${session.getAttribute("type")}")
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

        if (request.getParameter("saved") != null){
            model["success"] = "Federation Address updated successfully!"
        }
        return "dashboard"
    }
}