package de.diekautz.federationserver.controller.filter

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Order(1)
class CorsFilter: Filter {
    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        response as HttpServletResponse

        response.setHeader("Access-Control-Allow-Origin", "*")


        chain.doFilter(request, response)
    }


}
