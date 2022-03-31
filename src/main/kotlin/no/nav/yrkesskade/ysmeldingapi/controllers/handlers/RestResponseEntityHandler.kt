package no.nav.yrkesskade.ysmeldingapi.controllers.handlers

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import no.nav.yrkesskade.ysmeldingapi.exceptions.AltinnException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.ws.rs.BadRequestException
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotFoundException


@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [BadRequestException::class])
    protected fun handleConflict(ex: RuntimeException?, request: WebRequest?): ResponseEntity<Any> =
        handleExceptionInternal(ex!!, Feilmelding.fraException(ex), HttpHeaders(), HttpStatus.BAD_REQUEST, request!!)

    @ExceptionHandler(value = [NotFoundException::class])
    protected fun handleNotFound(ex: RuntimeException?, request: WebRequest?): ResponseEntity<Any> =
        handleExceptionInternal(ex!!, Feilmelding.fraException(ex), HttpHeaders(), HttpStatus.NOT_FOUND, request!!)


    @ExceptionHandler(value = [ForbiddenException::class])
    protected fun handleForbidden(ex: RuntimeException?, request: WebRequest?): ResponseEntity<Any> =
        handleExceptionInternal(ex!!, Feilmelding.fraException(ex), HttpHeaders(), HttpStatus.FORBIDDEN, request!!)

    @ExceptionHandler(value = [AltinnException::class])
    protected fun handleAltinnException(ex: AltinnException?, request: WebRequest?): ResponseEntity<Any> =
        handleExceptionInternal(ex!!, Feilmelding.fraException(ex), HttpHeaders(), HttpStatus.valueOf(ex.httpStatus), request!!)

    @ExceptionHandler(value = [InvalidFormatException::class])
    protected fun handleJacksonSerializationExceptions(ex: Exception?, request: WebRequest?): ResponseEntity<Any> =
        handleExceptionInternal(ex!!, Feilmelding.fraException(ex), HttpHeaders(), HttpStatus.BAD_REQUEST, request!!)

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleExceptionInternal(ex!!, Feilmelding.fraExceptionMedLocalizedMessage(ex), HttpHeaders(), HttpStatus.BAD_REQUEST, request!!)
}

data class Feilmelding(val melding: String) {
    companion object {
        fun fraException(exception: Throwable) = Feilmelding(exception.message.orEmpty())
        fun fraExceptionMedLocalizedMessage(exception: Throwable) = Feilmelding(exception.localizedMessage.orEmpty())
    }
}