package no.nav.yrkesskade.ysmeldingapi.clients

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.yrkesskade.ysmeldingapi.models.SkademeldingDto
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MottakClient(@Value("\${api.client.yrkesskade-mottak-url}") private val mottakBaseUrl: String) {

    private val client = OkHttpClient()

    fun sendTilMottak(skademelding: SkademeldingDto): SkademeldingDto {
        val postBody = jacksonObjectMapper().writeValueAsString(skademelding)
        val request = Request.Builder()
                .url("$mottakBaseUrl/api/skademelding/")
                .post(postBody.toRequestBody())
                .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("Unexpected code $response")
            return jacksonObjectMapper().readValue(response.body.toString())
        }
    }
}