package no.nav.yrkesskade.ysmeldingapi.exceptions

class AltinnException : Exception {
    var httpStatus: Int = 400

    constructor() : super()
    constructor(message: String, httpStatus: Int) : super(message)  {
        this.httpStatus = httpStatus
    }
    constructor(message: String, cause: Throwable, httpStatus: Int) : super(message, cause) {
        this.httpStatus = httpStatus
    }
    constructor(cause: Throwable, httpStatus: Int) : super(cause) {
        this.httpStatus = httpStatus
    }
}