FROM openjdk:8-alpine
MAINTAINER Yonghak An <neoshaby@gmail.com>

ARG FLUME_VER=1.9.0
ARG FLUME_BIN=apache-flume-${FLUME_VER}-bin.tar.gz

ENV TZ=Asia/Seoul

RUN apk --no-cache add ca-certificates bash tzdata curl && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

copy ./apk/sgerrand.rsa.pub /etc/apk/keys/sgerrand.rsa.pub
copy ./apk/glibc-bin-2.35-r0.apk /glibc-bin-2.35-r0.apk
copy ./apk/glibc-i18n-2.35-r0.apk /glibc-i18n-2.35-r0.apk
copy ./apk/glibc-2.35-r0.apk /glibc-2.35-r0.apk
copy ./apk/apache-flume-1.9.0-bin.tar.gz /apache-flume-1.9.0-bin.tar.gz

RUN apk add glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk

RUN /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    echo "export LC_ALL='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANG='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='ko_KR.UTF-8'" >> /etc/profile
ENV LC_ALL=ko_KR.UTF-8

RUN mkdir -p /opt/flume \
  && mkdir -p /opt/logs \
  && mkdir -p /opt/daemon \
  && mkdir -p /opt/daemon/lib \
  && mkdir -p /opt/daemon/config \
  && mkdir -p /opt/flume/plugins.d/agent \
  && tar -zxvf ${FLUME_BIN} -C /opt/flume --strip 1 \
  && rm -rf ${FLUME_BIN} \
  && rm -rf /opt/flume/lib/commons-compress-1.4.1.jar


WORKDIR /opt/flume

copy ./agent/ plugins.d/agent/
copy ./conf/ conf/
copy ./lib/ /opt/daemon/lib/
copy ./config/ /opt/daemon/config/

add ./lib/smartcity-core-0.9.1.jar plugins.d/agent/lib/smartcity-core-0.9.1.jar
add ./lib/smartcity-adapter-0.9.1.jar plugins.d/agent/lib/smartcity-adapter-0.9.1.jar
add ./lib/smartcity-flow-0.1.0.jar plugins.d/agent/lib/smartcity-flow-0.1.0.jar

run  chmod 755 plugins.d/agent/lib/compile.sh \
  && rm -rf /opt/daemon/lib/smartcity-flow-0.1.0.jar \
  && rm -rf /opt/daemon/lib/smartcity-adapter-0.9.1.jar


EXPOSE 8888

copy smartcity-daemon-0.9.0.jar /opt/daemon/app.jar


ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=local","-jar","/opt/daemon/app.jar"]


