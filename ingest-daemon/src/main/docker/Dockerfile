FROM openjdk:8-slim-bullseye
LABEL name="Yonghak.An"
LABEL email="neoshaby@gmail.com"

ENV TZ=Asia/Seoul

COPY ./apk/ /

RUN apt-get update && apt-get install -y locales \
  && localedef -f UTF-8 -i ko_KR ko_KR.UTF-8 \
  && ln -fs /usr/share/zoneinfo/$TZ /etc/localtime   \
  && echo $TZ > /etc/timezone \
  && mkdir -p /opt/logs \
  && mkdir -p /opt/daemon/config \
  && mkdir -p /opt/flume/plugins.d/agent \
  && tar -zxvf apache-flume-1.9.0-bin.tar.gz -C /opt/flume --strip-components=1 \
  && rm -rf apache-flume-1.9.0-bin.tar.gz \
  && rm -rf /opt/flume/lib/commons-compress-1.4.1.jar

ENV LC_ALL ko_KR.UTF-8
ENV PYTHONIOENCODING=utf-8
EXPOSE 8888

WORKDIR /opt/flume

COPY ./agent/ plugins.d/agent/
COPY ./conf/ conf/
ADD ./config/application.yaml /opt/daemon/config/application.yaml
ADD ./config/log4j2.xml /opt/daemon/config/log4j2.xml

RUN  chmod 755 plugins.d/agent/lib/compile.sh 


COPY ingest-daemon-0.9.0.jar /opt/daemon/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=dev","-jar","/opt/daemon/app.jar"]
