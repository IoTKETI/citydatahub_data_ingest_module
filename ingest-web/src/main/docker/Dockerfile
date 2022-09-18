FROM openjdk:8-alpine
LABEL name="Yonghak.An"
LABEL email="neoshaby@gmail.com"

ENV TZ=Asia/Seoul

RUN apk --no-cache add ca-certificates bash tzdata curl && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

COPY ./apk/sgerrand.rsa.pub /etc/apk/keys/sgerrand.rsa.pub
COPY ./apk/ /

RUN apk add glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk

# install ko_KR locale
RUN /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    echo "export LC_ALL='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANG='ko_KR.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='ko_KR.UTF-8'" >> /etc/profile
ENV LC_ALL=ko_KR.UTF-8

RUN mkdir -p /opt/web/config

COPY ./config/ /opt/web/config/

WORKDIR /opt/web

EXPOSE 8080
  
ADD ingest-web-1.0.0.jar /opt/web/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar","/opt/web/app.jar"]

