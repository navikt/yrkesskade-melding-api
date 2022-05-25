## Yrkesskade-melding-api

Yrkesskade-melding-api er et REST API som tar imot yrkesskademeldinger. Det er lagt opp til å benyttes på to måter:
- Intern, direkte konsumering av NAV sin egen frontend-applikasjon for yrkesskadeinnmelding
- Ekstern konsumering av tredjepartsaktører via [yrkesskade-ekstern-gateway](https://github.com/navikt/yrkesskade-ekstern-gateway)

REST APIet er dokumentert med en [Swaggerdoc](https://raw.githubusercontent.com/navikt/yrkesskade-backend-felles/master/skademelding/src/main/resources/openapi.yaml) 
som følger OpenAPI 3-standarden.

APIet er bygget opp slik at det som tidligere var flere forskjellige typer skademeldinger (arbeidstaker, elev, militær, etc.) nå kan dekkes av ett og samme endepunkt med en felles datamodell.
For å muliggjøre dette, bruker vi et [kodeverk](https://github.com/navikt/yrkesskade-kodeverk) som spesifiserer gyldige verdier for de forskjellige feltene per skademeldingstype.

## Tilgang
### For interne konsumenter
Interne konsumenter må autentisere seg med Azure AD, type server–server, som beskrevet i [nais-dokumentasjonen](https://doc.nais.io/security/auth/azure-ad/).

### For eksterne konsumenter
Eksterne konsumenter må autentisere seg med et Maskinporten-token for å kunne sende inn skademeldinger. 
Vi som API-tilbydere må registrere konsumenten sitt organisasjonsnummer for at denne autentiseringen skal fungere. 
Maskinporten har en egen guide for hvordan man kan [sette opp tilgang](https://docs.digdir.no/docs/Maskinporten/maskinporten_guide_apikonsument).
Det mest aktuelle per nå er å sette opp [delegert tilgang](https://docs.digdir.no/docs/Maskinporten/maskinporten_func_delegering.html), 
slik at en organisasjon kan sende inn yrkesskademeldinger via en tredjeparts leverandørsystem.
Eksterne konsumenter må som nevnt gå via URLen til `yrkesskade-ekstern-gateway` for å sende inn yrkesskademeldinger til oss.
Dette er måten å tilgjengeliggjøre vårt API til internett på, ved å kreve et gyldig Maskinporten-token med egendefinerte scopes.
Dokumentasjon av denne gatewayen kommer snart.
Ta kontakt med oss på yrkesskade@nav.no for å bli registrert hos oss som godkjent Maskinporten-konsument.

## URLer
Det er forskjellige URLer avhengig av om konsumerende applikasjon er intern (NAV) eller ekstern.  

| miljø | intern                                           | ekstern                                       |
|-------|--------------------------------------------------|-----------------------------------------------|
| DEV   | https://yrkesskade-melding-api.dev.intern.nav.no | https://yrkesskade-ekstern-gateway.dev.nav.no |
| PROD  | https://yrkesskade-melding-api.intern.nav.no     | https://yrkesskade-ekstern-gateway.nav.no     |