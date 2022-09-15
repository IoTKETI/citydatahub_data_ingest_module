FROM openjdk:8-alpine
MAINTAINER Yonghak An <neoshaby@gmail.com>

ENV TZ=Asia/Seoul

RUN apk --no-cache add ca-certificates bash tzdata curl && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

copy ./apk/sgerrand.rsa.pub /etc/apk/keys/sgerrand.rsa.pub
copy ./apk/glibc-bin-2.35-r0.apk /glibc-bin-2.35-r0.apk
copy ./apk/glibc-i18n-2.35-r0.apk /glibc-i18n-2.35-r0.apk
copy ./apk/glibc-2.35-r0.apk /glibc-2.35-r0.apk

RUN apk add glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk

# install ko_KR locale
RUN /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    echo "export LC_ALL='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANG='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='ko_KR.UTF-8'" >> /etc/profile
ENV LC_ALL=ko_KR.UTF-8

run mkdir -p /opt/web \
  && mkdir -p /opt/web/config

copy ./config/ /opt/web/config/

workdir /opt/web

EXPOSE 8080
  
ADD smartcity-web-1.0.0.jar /opt/web/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=local","-jar","/opt/web/app.jar"]

