package de.diekautz.federationserver.controller

import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Controller
class ApplicationErrorController: ErrorController {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @RequestMapping("/error")
    fun handleError(response: HttpServletResponse, request: HttpServletRequest) {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        if (status != null) {
            log.info("Failed Error")
        }
    }

}