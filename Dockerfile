FROM navikt/java:11-appdynamics

ENV APPLICATION_NAME=yrkesskade-melding-api
ENV APPD_ENABLED=TRUE
ENV JAVA_OPTS="-Dhttps.protocols=TLSv1,TLSv1.1,TLSv1.2 -XX:+UseParallelGC -XX:MaxRAMPercentage=75.0"

COPY ./target/yrkesskade-melding-api-0.0.1-SNAPSHOT.jar "app.jar"