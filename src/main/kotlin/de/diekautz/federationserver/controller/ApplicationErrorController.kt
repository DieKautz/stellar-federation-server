package de.diekautz.federationserver.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
class ApplicationErrorController: ErrorController {

    @RequestMapping("/error")
    fun handleError(): String {
        return "login"
    }

}