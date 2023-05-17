ARG APP_INSIGHTS_AGENT_VERSION=2.5.1-BETA
FROM hmctspublic.azurecr.io/base/java:11-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ia-hearings-api.jar /opt/app/

EXPOSE 8100

CMD [ "ia-hearings-api.jar" ]
