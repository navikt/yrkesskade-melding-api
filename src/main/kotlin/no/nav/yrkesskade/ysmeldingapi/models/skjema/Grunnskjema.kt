package no.nav.yrkesskade.ysmeldingapi.models.skjema

import no.bekk.bekkopen.person.FodselsnummerValidator
import no.nav.yrkesskade.skademelding.model.Periode
import no.nav.yrkesskade.skademelding.model.SkadetDel
import no.nav.yrkesskade.skademelding.model.Tidstype
import no.nav.yrkesskade.ysmeldingapi.config.FeatureToggles
import no.nav.yrkesskade.ysmeldingapi.models.skjema.SkjemaUtils.Companion.erPeriode
import no.nav.yrkesskade.ysmeldingapi.models.skjema.SkjemaUtils.Companion.rolletype
import java.time.OffsetDateTime

class Grunnskjema(private val skjemaContext: SkjemaContext) : Innmeldingsskjema {

    override fun valider() {
        val skademelding = skjemaContext.skademelding

        // null sjekk
        checkNotNull(skademelding.skadelidt, {"skadelidt er påkrevd"})
        checkNotNull(skademelding.hendelsesfakta, {"hendelsesfakta er påkrevd"})
        checkNotNull(skademelding.skade, {"skade er påkrevd"})
        check(skademelding.innmelder.innmelderrolle == "virksomhetsrepresentant", { "${skademelding.innmelder.innmelderrolle} er ikke en gyldig verdi. Må være virksomhetsrepresentant"})

        // Hent kodelister basert på rolletype som skal benyttes for å finne gyldige verdier
        checkNotNull(skademelding.skadelidt.dekningsforhold.rolletype, { "rolletype er påkrevd" })

        val rolletype = rolletype(skademelding)

        skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForType(rolletype, "rolletype", "${rolletype} er ikke en gyldig rolletype kode i kodelisten")

        // sjekk organisasjon
        check(!skademelding.innmelder.paaVegneAv.isNullOrBlank(), {"paaVegneAv er påkrevd"})
        check(!skademelding.skadelidt.dekningsforhold.organisasjonsnummer.isNullOrBlank(), {"organisasjonsnummer er påkrevd"})

        val paaVegneAvOrganisasjon = skjemaContext.enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret(skademelding.innmelder.paaVegneAv)
        check(!paaVegneAvOrganisasjon.organisasjonsnummer.isNullOrBlank(), {"Ugyldig innmelder.paaVegneAv enhet. ${skademelding.innmelder.paaVegneAv} er finnes ikke i enhetsregisteret"})

        val dekningsforholdOrganisasjon = skjemaContext.enhetsregisterClient.hentEnhetEllerUnderenhetFraEnhetsregisteret(skademelding.skadelidt.dekningsforhold.organisasjonsnummer)
        check(!dekningsforholdOrganisasjon.organisasjonsnummer.isNullOrBlank(), {"Ugyldig dekningsforhold.organisasjonsnummer enhet. ${skademelding.skadelidt.dekningsforhold.organisasjonsnummer} finnes ikke i enhetsregisteret"})

        // sjekk fødselsnumre i produksjon
        if (!skjemaContext.featureToggleService.isEnabled(FeatureToggles.ER_IKKE_PROD.toggleId)) {
            check(
                FodselsnummerValidator.isValid(skademelding.innmelder.norskIdentitetsnummer),
                { "innmelder.norskIdentitetsnummer er ugyldig. ${skademelding.innmelder.norskIdentitetsnummer} er ikke gyldig norsk person identitetsnummer" })
            check(
                FodselsnummerValidator.isValid(skademelding.skadelidt.norskIdentitetsnummer),
                { "skadelidt.norskIdentitetsnummer er ugyldig. ${skademelding.skadelidt.norskIdentitetsnummer} er ikke gyldig norsk person identitetsnummer" })
        }
        // valider at norskIdentitetsnumre er ulike
        check(!skademelding.innmelder.norskIdentitetsnummer.equals(skademelding.skadelidt.norskIdentitetsnummer),
            {"innsenders norsk identitetsnummer kan ikke være det samme som skadelidtes norske identitetsnummer"})

        // valider tidstype
        checkNotNull(skademelding.hendelsesfakta.tid.tidstype, {"hendelsesfakta.tid.tidstype er påkrevd"})
        when (skademelding.hendelsesfakta.tid.tidstype) {
            Tidstype.tidspunkt -> validerTidspunkt(skademelding.hendelsesfakta.tid.tidspunkt)
            Tidstype.periode -> validerTidsperioder(skademelding.hendelsesfakta.tid.perioder)
        }

        // felter som skal valideres
        val kodelisteOgVerdi = mutableListOf(
            Pair("tidsrom", skademelding.hendelsesfakta.naarSkjeddeUlykken),
        )

        // dersom tidstype er periode - valider sykdomsfelter
        if (skademelding.hendelsesfakta.tid.tidstype == Tidstype.periode) {
            skademelding.hendelsesfakta.paavirkningsform!!.forEach {
                skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForType(
                    it,
                    "paavirkningsform",
                    "${it} er ikke en gyldig paavirkningsform verdi. Sjekk kodeliste for gyldige verdier")
            }
        }

        validerSkadedeDeler(skademelding.skade.skadedeDeler)

        if (!erPeriode(skademelding)) {
            checkNotNull(skademelding.hendelsesfakta.aarsakUlykke, { "aarsakUlykke er påkrevd ved yrkesskade"})
            skademelding.hendelsesfakta.aarsakUlykke!!.forEach {
                kodelisteOgVerdi.add(Pair("aarsakOgBakgrunn", it))
            }
        }

        if (skademelding.skade.alvorlighetsgrad != null) {
            kodelisteOgVerdi.add(Pair("alvorlighetsgrad", skademelding.skade!!.alvorlighetsgrad!!))
        }

        // rolletype benyttes som kategori navn (elev, arbeidstaker, laerling osv)
        kodelisteOgVerdi.forEach {
            skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForTypeOgKategori(
                it.second,
                it.first,
                rolletype,
                "${it.second} er ikke en gyldig ${it.first} verdi. Sjekk kodeliste for gyldige verdier"
            )
        }
    }

    private fun validerTidsperioder(perioder: List<Periode>?) {
        checkNotNull(perioder, { "hendelsesfakta.tid.perioder er påkrevd" })
        perioder.forEach {
            checkNotNull(it.fra, { "hendelsesfakta.tid.perioder[*].fra er påkrevd" })
            checkNotNull(it.til, { "hendelsesfakta.tid.perioder[*].til er påkrevd" })
            check(
                it.fra!!.isBefore(it.til!!) || it.fra!!.isEqual(it.til!!),
                { "fra dato må være før eller sammme som til dato" })

        }
    }

    private fun validerTidspunkt(tidspunkt: OffsetDateTime?) {
        checkNotNull(tidspunkt, {"hendelsesfakta.tid.tidspunkt er påkrevd"})
    }

    private fun validerSkadedeDeler(skadedeDeler: List<SkadetDel>) {
        check(skadedeDeler.isNotEmpty(), {"skadedeDeler kan ikke være tom"})

        skadedeDeler.forEach {
            skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForTyper(it.skadeart,"${it.skadeart} er ikke en gyldig verdi. Sjekk kodelistene skadetype og sykdomstype for gyldige verdier", "skadetype", "sykdomstype" )
            skjemaContext.kodeverkValidator.sjekkGyldigKodeverkverdiForType(it.kroppsdel, "skadetKroppsdel", "${it.kroppsdel} er ikke en gyldig verdi. Sjekk kodelisten skadetKroppsdel for gyldige verdier")
        }
    }
}