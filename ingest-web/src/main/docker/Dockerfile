FROM openjdk:8-slim-bullseye
LABEL name="Yonghak.An"
LABEL email="neoshaby@gmail.com"

ENV TZ=Asia/Seoul

RUN apt-get update && apt-get install -y locales && \
    localedef -f UTF-8 -i ko_KR ko_KR.UTF-8 && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo $TZ > /etc/timezone && \
    mkdir -p /opt/web/config

ENV LC_ALL ko_KR.UTF-8
ENV PYTHONIOENCODING=utf-8

EXPOSE 8080

WORKDIR /opt/web

COPY ./config/ /opt/web/config/
  
ADD ingest-web-1.0.0.jar /opt/web/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Djdk.io.File.enableADS=true", "-jar","/opt/web/app.jar"]

