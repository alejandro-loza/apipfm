FROM adoptopenjdk/openjdk9-openj9:jdk-9.0.4.12_openj9-0.9.0-alpine
COPY build/libs/finerio-pfm-api-*-all.jar finerio-pfm-api.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xms4096m", "-Xmx4096m", "-XX:+IdleTuningGcOnIdle", "-XX:MaxDirectMemorySize=512m", "-XX:-UseGCOverheadLimit","-Xtune:virtualized", "-server", "-jar", "finerio-pfm-api.jar"]