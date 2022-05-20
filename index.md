## Yrkesskade-melding-api

Yrkesskade-melding-api er et REST API som tar imot yrkesskademeldinger. Det er lagt opp til å benyttes på to måter:
- Intern konsumering av NAV sin egen frontend-applikasjon for yrkesskadeinnmelding
- Ekstern konsumering av tredjepartsaktører via [yrkesskade-ekstern-gateway](https://github.com/navikt/yrkesskade-ekstern-gateway)

REST APIet er dokumentert med en [Swaggerdoc](https://raw.githubusercontent.com/navikt/yrkesskade-backend-felles/master/skademelding/src/main/resources/openapi.yaml) som følger OpenAPI 3-standarden.

## Tilgang for eksterne konsumenter
Eksterne konsumenter må autentisere seg med et Maskinporten-token for å kunne sende inn skademeldinger. Deretter må vi som API-tilbydere registrere konsumenten sitt organisasjonsnummer. Maskinporten har en egen guide for hvordan man kan [sette opp tilgang](https://docs.digdir.no/docs/Maskinporten/maskinporten_guide_apikonsument).

Eksterne konsumenter må som nevnt bruke `yrkesskade-ekstern-gateway` for å sende inn yrkesskademeldinger til oss. Dette er måten å tilgjengeliggjøre vårt API til internett på, ved å kreve et gyldig Maskinporten-token med egendefinerte scopes. Dokumentasjon av denne gatewayen kommer snart.

