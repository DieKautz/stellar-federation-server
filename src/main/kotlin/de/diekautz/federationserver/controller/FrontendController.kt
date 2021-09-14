package de.diekautz.federationserver.controller

import de.diekautz.federationserver.config.FederationConfiguration
import de.diekautz.federationserver.controller.SessionType.*
import de.diekautz.federationserver.datasource.FederationAddressDataSource
import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.model.UserSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
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

    @ExceptionHandler(Exception::class)
    fun handleOtherEx(e: Exception): RedirectView {
        log.error("An unhandled error occurred! ${e.message}\n${e.stackTraceToString()}")
        return RedirectView("/")
    }

    @GetMapping("/")
    fun serveHomePage(request: HttpServletRequest, session: HttpSession): RedirectView {

        return if (session.getAttribute("id") == null)
            RedirectView("/dashboard")
        else RedirectView("/login")

    }

    @GetMapping("/login")
    fun serveLoginPage(model: Model, session: HttpSession, request: HttpServletRequest, redirAttr: RedirectAttributes): String {
        val userSession = session.getAttribute("user") as UserSession?
        if(userSession == null){
            session.setAttribute("user", UserSession())
        } else if (userSession.sessionType != NONE) {
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
        val userSession = session.getAttribute("user") as UserSession?
        if (userSession == null || userSession.sessionType == NONE) {
            return "redirect:/login"
        }
        log.debug("Logout requested for session: ${userSession.id}")
        session.setAttribute("user", null)
        session.invalidate()
        model["success"] = "Log out successful!"
        return "login"
    }

    @GetMapping("/dashboard")
    fun serveDashboard(model: Model, session: HttpSession, request: HttpServletRequest, redirAttr: RedirectAttributes): String {
        val userSession = session.getAttribute("user") as UserSession?
        if (userSession == null || userSession.sessionType == NONE) {
            return "redirect:/login"
        }
        model["sessionId"] = userSession.id.toString()
        model["type"] = userSession.sessionType
        model["socialName"] = userSession.username

        when (userSession.sessionType) {
            DISCORD -> {
                model["endpoint"] = "/api/discord"
            }
            GITHUB -> {
                model["endpoint"] = "/api/github"
            }
            TWITTER -> {
                model["endpoint"] = "/api/twitter"
            }
            else -> {}
        }
        var fedAddress = dataSource.findAddress("${userSession.username}*${userSession.sessionType.subDomain}.${fedConfig.domain}")
        if (fedAddress == null) {
            fedAddress = FederationAddress("${userSession.username}*${userSession.sessionType.subDomain}.${fedConfig.domain}", "")
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