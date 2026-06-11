ARG APP_INSIGHTS_AGENT_VERSION=3.7.3
FROM hmctsprod.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ia-hearings-api.jar /opt/app/

EXPOSE 8100

CMD [ "ia-hearings-api.jar" ]
