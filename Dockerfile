ARG APP_INSIGHTS_AGENT_VERSION=3.2.10
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/ia-hearings-api.jar /opt/app/

EXPOSE 8100
CMD [ "ia-hearings-api.jar" ]
