package no.nav.yrkesskade.ysmeldingapi.controllers.handlers

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import no.nav.yrkesskade.ysmeldingapi.exceptions.AltinnException
import no.nav.yrkesskade.ysmeldingapi.utils.getLogger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.invoke.MethodHandles
import javax.ws.rs.BadRequestException
import javax.ws.rs.ForbiddenException
import javax.ws.rs.NotFoundException

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = getLogger(MethodHandles.lookup().lookupClass())

    @ExceptionHandler(value = [BadRequestException::class])
    protected fun handleConflict(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> =
        handleExceptionAndLogError(ex, request, HttpHeaders(), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(value = [NotFoundException::class])
    protected fun handleNotFound(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> =
        handleExceptionAndLogError(ex, request, HttpHeaders(), HttpStatus.NOT_FOUND)


    @ExceptionHandler(value = [ForbiddenException::class])
    protected fun handleForbidden(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> =
        handleExceptionAndLogError(ex, request, HttpHeaders(), HttpStatus.FORBIDDEN)

    @ExceptionHandler(value = [AltinnException::class])
    protected fun handleAltinnException(ex: AltinnException, request: WebRequest): ResponseEntity<Any> =
        handleExceptionAndLogError(ex, request, HttpHeaders(), HttpStatus.valueOf(ex.httpStatus))

    @ExceptionHandler(value = [InvalidFormatException::class])
    protected fun handleJacksonSerializationExceptions(ex: Exception, request: WebRequest): ResponseEntity<Any> =
        handleExceptionAndLogError(ex, request, HttpHeaders(), HttpStatus.BAD_REQUEST)

    @ExceptionHandler(value = [Exception::class])
    protected fun handleAnyException(ex: Exception, request: WebRequest): ResponseEntity<Any> =
        handleExceptionAndLogError(ex, request, HttpHeaders(), HttpStatus.BAD_REQUEST)

    private fun handleExceptionAndLogError(ex: Exception, request: WebRequest, headers: HttpHeaders, httpStatus: HttpStatus): ResponseEntity<Any> {
        log.error("${ex.javaClass.simpleName}: ${ex.message} \n${ex.stackTraceToString()}")
        return handleExceptionInternal(ex, Feilmelding.fraException(ex), headers, httpStatus, request)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> = handleExceptionAndLogError(ex, request, headers, HttpStatus.BAD_REQUEST)
}

data class Feilmelding(val melding: String) {
    companion object {
        fun fraException(exception: Throwable) = Feilmelding(exception.message.orEmpty())
        fun fraExceptionMedLocalizedMessage(exception: Throwable) = Feilmelding(exception.localizedMessage.orEmpty())
    }
}