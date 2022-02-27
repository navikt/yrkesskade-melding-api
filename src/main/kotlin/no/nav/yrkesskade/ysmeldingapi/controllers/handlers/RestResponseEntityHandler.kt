package no.nav.yrkesskade.ysmeldingapi.controllers.handlers

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.ws.rs.BadRequestException
import javax.ws.rs.ForbiddenException


@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [BadRequestException::class])
    protected fun handleConflict(ex: RuntimeException?, request: WebRequest?): ResponseEntity<Any> =
        handleExceptionInternal(ex!!, Feilmelding.fraException(ex), HttpHeaders(), HttpStatus.BAD_REQUEST, request!!)

    @ExceptionHandler(value = [ForbiddenException::class])
    protected fun handleForbidden(ex: RuntimeException?, request: WebRequest?): ResponseEntity<Any> =
        handleExceptionInternal(ex!!, Feilmelding.fraException(ex), HttpHeaders(), HttpStatus.FORBIDDEN, request!!)
}

data class Feilmelding(val melding: String) {
    companion object {
        fun fraException(exception: RuntimeException) = Feilmelding(exception.message.orEmpty())
    }
}