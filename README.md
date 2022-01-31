# yrkesskade-melding-api
API for innsending av melding om yrkesskade, -sykdom og menerstatning.

## Oppsett i IDE
Det blir generert filer av maven, som blir lagt inn i target/generated-sources. Denne mappen må legges til som source i IDEen.
- IntelliJ: høyreklikk på generated-sources -> Mark Directory as -> Generated sources root

## Lokal kjøring
Applikasjonen kan startes ved å kjøre YrkesskadeMeldingApiApplication.

Legg til VM argumentene `-DYRKESSKADE_API_DB_USERNAME=<brukernavn>`og `-DYRKESSKADE_API_DB_PASSWORD=<passord>`

Spring profilen `local` må aktiveres med VM argument `-Dspring.profiles.active=local` eller ved hjelp av Active profile feltet i IntelliJ.

Database må kjøre før applikasjonen kan startes

### Database
Det forutsettes at det kjører en database lokalt. Vi bruker Postgres.

Dette kan enten installeres lokalt på maskinen eller startes med docker

```sql
CREATE DATABASE yrkesskade_melding
```

### Lokal API kall
Tjenesten krever at brukeren er pålogget før en kan bruke APIet.

#### Lokal
1. Gjør et kall mot http://localhost:{serverport}/local/cookie
2. Send cookie fra steg nummer 1 med kall mot API.
