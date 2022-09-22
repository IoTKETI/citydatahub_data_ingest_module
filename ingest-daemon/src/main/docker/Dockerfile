FROM openjdk:8-alpine
LABEL name="Yonghak.An"
LABEL email="neoshaby@gmail.com"

ARG FLUME_VER=1.9.0
ARG FLUME_BIN=apache-flume-${FLUME_VER}-bin.tar.gz

ENV TZ=Asia/Seoul

RUN apk --no-cache add ca-certificates bash tzdata curl && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

COPY ./apk/sgerrand.rsa.pub /etc/apk/keys/sgerrand.rsa.pub
COPY ./apk/ /

RUN apk add glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk

RUN /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    echo "export LC_ALL='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANG='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='ko_KR.UTF-8'" >> /etc/profile
ENV LC_ALL=ko_KR.UTF-8

RUN mkdir -p /opt/logs \
  && mkdir -p /opt/daemon/config \
  && mkdir -p /opt/flume/plugins.d/agent \
  && tar -zxvf ${FLUME_BIN} -C /opt/flume --strip 1 \
  && rm -rf ${FLUME_BIN} \
  && rm -rf /opt/flume/lib/commons-compress-1.4.1.jar

WORKDIR /opt/flume

COPY ./agent/ plugins.d/agent/
COPY ./conf/ conf/
ADD ./config/application.yaml /opt/daemon/config/application.yaml
ADD ./config/log4j2.xml /opt/daemon/config/log4j2.xml

RUN  chmod 755 plugins.d/agent/lib/compile.sh 

EXPOSE 8888

COPY ingest-daemon-0.9.0.jar /opt/daemon/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=dev","-jar","/opt/daemon/app.jar"]