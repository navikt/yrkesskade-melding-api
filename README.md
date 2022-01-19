# yrkesskade-melding-api
API for innsending av melding om yrkesskade, -sykdom og menerstatning.

## Lokal kjøring
Applikasjonen kan startes ved å kjøre YrkesskadeMeldingMottakApplication.

Legg til VM argumentene `-DYRKESSKADE_API_DB_USERNAME=<brukernavn>`og `-DYRKESSKADE_API_DB_PASSWORD=<passord>`

Spring profilen `local` må aktiveres med VM argument `-Dspring.profiles.active=local` eller ved hjelp av Active profile feltet i IntelliJ.

Database må kjøre før applikasjonen kan startes

### Database
Det forutsettes at det kjører en database lokalt. Vi bruker Postgres.

Dette kan enten installeres lokalt på maskinen eller startes med docker

```sql
CREATE DATABASE yrkesskade_melding
```
