package de.diekautz.federationserver.controller

import de.diekautz.federationserver.model.FederationAddress
import de.diekautz.federationserver.service.FederationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/federation")
class FederationController(private val service: FederationService) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<String> =
        ResponseEntity("{\"error\":\"${e.message}\"}", HttpStatus.NOT_FOUND)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<String> =
        ResponseEntity("{\"error\":\"${e.message}\"}", HttpStatus.BAD_REQUEST)

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleEx(e: MissingServletRequestParameterException): ResponseEntity<String> =
        ResponseEntity("{\"error\":\"${e.message}\"}", HttpStatus.BAD_REQUEST)

    @GetMapping
    fun getFedAddr(@RequestParam("type") type: String, @RequestParam("q") query: String, response: HttpServletResponse): FederationAddress {
        when(type) {
            "name" -> {
                response.setHeader("Access-Control-Allow-Origin", "*")
                return service.getByFedAddress(query)
            }
            else -> {
                throw IllegalArgumentException("Request type '$type' not supported.")
            }
        }
    }

    @GetMapping("/all")
    fun getAll(): Collection<FederationAddress> {
        return service.getAddresses()
    }

    /*@PostMapping("/edit")
    fun createFedAddr(@RequestBody address: FederationAddress): FederationAddress = service.createFedAddress(address)

    @PutMapping("/edit")
    fun updateFedAddr(
        @RequestBody address: FederationAddress): FederationAddress
            = service.updateFedAddress(address)

    @DeleteMapping("/edit/{addr}")
    fun deleteFedAddr(@PathVariable addr: String): ResponseEntity<Any?> {
        service.deleteFedAddress(addr)
        return ResponseEntity("{\"success\":\"Federation address successfully deleted!\"}", HttpStatus.NO_CONTENT)
    }*/
}