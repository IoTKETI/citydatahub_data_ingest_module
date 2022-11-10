FROM eclipse-temurin:8-jdk-alpine  
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
    echo "export LANGUAGE='C.UTF-8'" >> /etc/profile
  
RUN mkdir -p /opt/logs \
  && mkdir -p /opt/daemon/config \
  && mkdir -p /opt/flume/plugins.d/agent \
  && tar --extract --file apache-flume-1.11.0-bin.tar.gz --directory /opt/flume --strip-components 1 --no-same-owner \ 
  && rm -rf apache-flume-1.11.0-bin.tar.gz glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk 

ENV LC_ALL=C.UTF-8
EXPOSE 8888

WORKDIR /opt/flume

COPY ./agent/ plugins.d/agent/
COPY ./conf/ conf/
ADD ./config/application.yaml /opt/daemon/config/application.yaml
ADD ./config/log4j2.xml /opt/daemon/config/log4j2.xml

RUN sed -i 's/\r$//' conf/log4j2.xml \
  && sed -i 's/\r$//' conf/flume-env.sh \
  && sed -i 's/\r$//' plugins.d/agent/lib/compile.sh \
  && chmod 755 plugins.d/agent/lib/compile.sh 


COPY ingest-daemon-1.0.0.jar /opt/daemon/app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=dev","-jar","/opt/daemon/app.jar"]
