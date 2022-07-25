ARG APP_INSIGHTS_AGENT_VERSION=3.2.10
FROM hmctspublic.azurecr.io/base/java:11-distroless

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ia-hearings-api.jar /opt/app/

EXPOSE 8100

CMD [ "ia-hearings-api.jar" ]
