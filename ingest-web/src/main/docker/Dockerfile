FROM eclipse-temurin:11-jdk-alpine 
LABEL name="Yonghak.An"
LABEL email="neoshaby@gmail.com"

ENV TZ=Asia/Seoul

COPY ./apk/sgerrand.rsa.pub /etc/apk/keys/sgerrand.rsa.pub
COPY ./apk/ /

RUN apk --no-cache add ca-certificates tzdata curl && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

RUN apk add glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk && \
    /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    /usr/glibc-compat/bin/localedef -i ko_KR -f EUC-KR ko_KR.EUC-KR && \
    echo "export LC_ALL='C.UTF-8'" >> /etc/profile && \
    echo "export LANG='C.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='C.UTF-8'" >> /etc/profile && \
    rm -rf glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk
 
ENV LC_ALL=C.UTF-8

EXPOSE 8080

RUN mkdir -p /opt/web/config

WORKDIR /opt/web

COPY ./config/ /opt/web/config/
  
ADD ingest-web-1.0.0.jar /opt/web/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Djdk.io.File.enableADS=true", "-jar","/opt/web/app.jar"]

