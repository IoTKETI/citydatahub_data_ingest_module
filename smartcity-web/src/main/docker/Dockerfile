FROM openjdk:8-alpine
MAINTAINER Yonghak An <neoshaby@gmail.com>

RUN apk --no-cache add ca-certificates bash wget tzdata curl && \
    wget -q -O /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-2.33-r0.apk && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-bin-2.33-r0.apk && \
    wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.33-r0/glibc-i18n-2.33-r0.apk && \
    apk add glibc-bin-2.33-r0.apk glibc-i18n-2.33-r0.apk glibc-2.33-r0.apk

# install ko_KR locale
# Note that locale -a is not available in alpine linux, use `/usr/glibc-compat/bin/locale -a` instead
RUN /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && echo "Asia/Seoul" > /etc/timezone && \
    echo "export LC_ALL='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANG='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='ko_KR.UTF-8'" >> /etc/profile
ENV LC_ALL=ko_KR.UTF-8



VOLUME /tmp

run mkdir -p /opt/web
run mkdir -p /opt/web/config
ADD smartcity-web-1.0.0.jar /opt/web/app.jar

add ./config/application.yaml /opt/web/config/application.yaml
add ./config/log4j2.xml /opt/web/config/log4j2.xml

workdir /opt/web

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=local","-jar","/opt/web/app.jar"]

