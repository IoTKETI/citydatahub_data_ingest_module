FROM openjdk:8-alpine
MAINTAINER Yonghak An <neoshaby@gmail.com>

ARG MIRROR=http://archive.apache.org/dist/flume
ARG VERSION=1.9.0
ARG BIN_VERSION=apache-flume-${VERSION}

RUN apk --no-cache add ca-certificates bash wget tzdata curl && \
    wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-2.33-r0.apk && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-bin-2.33-r0.apk && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-i18n-2.33-r0.apk && \
    apk add glibc-bin-2.33-r0.apk glibc-i18n-2.33-r0.apk glibc-2.33-r0.apk

# install ko_KR locale
# Note that locale -a is not available in alpine linux, use `/usr/glibc-compat/bin/locale -a` instead
RUN /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime &&  echo "Asia/Seoul" > /etc/timezone && \
    echo "export LC_ALL='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANG='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='ko_KR.UTF-8'" >> /etc/profile
ENV LC_ALL=ko_KR.UTF-8

RUN mkdir -p /opt/flume \
  && mkdir -p /opt/logs \
  && mkdir -p /opt/flume/plugins.d/agent \
  && wget -q ${MIRROR}/${VERSION}/${BIN_VERSION}-bin.tar.gz \
  && tar -zxvf ${BIN_VERSION}-bin.tar.gz -C /opt/flume --strip 1 \
  && rm -rf ${BIN_VERSION}.tgz \
  && rm -rf /opt/flume/lib/commons-compress-1.4.1.jar

VOLUME /tmp


WORKDIR /opt/flume

copy ./agent/ /opt/flume/plugins.d/agent/
copy ./conf/ /opt/flume/conf/

run chmod 777 /opt/flume/plugins.d/agent/lib/compile.sh

run mkdir -p /opt/daemon
run mkdir -p /opt/daemon/config
ADD smartcity-daemon-0.9.0.jar /opt/daemon/app.jar
add ./config/application.yaml /opt/daemon/config/application.yaml
add ./config/log4j2.xml /opt/daemon/config/log4j2.xml



ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=local","-jar","/opt/daemon/app.jar"]


