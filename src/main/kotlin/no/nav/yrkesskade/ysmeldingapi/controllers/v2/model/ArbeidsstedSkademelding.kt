package no.nav.yrkesskade.ysmeldingapi.controllers.v2.model

class ArbeidsstedSkademelding(
    rolletype: String,
    innmelder: Innmelder) : Skademelding(rolletype, innmelder) {
}