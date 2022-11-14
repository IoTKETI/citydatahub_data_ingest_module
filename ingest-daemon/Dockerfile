FROM eclipse-temurin:8-jdk-alpine  
LABEL name="Yonghak.An"
LABEL email="neoshaby@gmail.com"

COPY src/main/docker/apk/sgerrand.rsa.pub /etc/apk/keys/sgerrand.rsa.pub
COPY src/main/docker/apk/ /

RUN apk --no-cache add ca-certificates tzdata curl && \
    apk add glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk && \
    /usr/glibc-compat/bin/localedef -i ko_KR -f UTF-8 ko_KR.UTF-8 && \
    /usr/glibc-compat/bin/localedef -i ko_KR -f EUC-KR ko_KR.EUC-KR && \
    echo "export LC_ALL='C.UTF-8'" >> /etc/profile && \
    echo "export LANG='C.UTF-8'" >> /etc/profile && \
    echo "export LANGUAGE='C.UTF-8'" >> /etc/profile

ARG TZ
ARG U_ID
ARG G_ID
ARG USER
ARG JAR_FILE

RUN cp /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone
  
RUN mkdir -p /opt/logs \
  && mkdir -p /opt/daemon/config \
  && mkdir -p /opt/flume/plugins.d/agent \
  && tar --extract --file apache-flume-1.11.0-bin.tar.gz --directory /opt/flume --strip-components 1 --no-same-owner \ 
  && rm -rf apache-flume-1.11.0-bin.tar.gz glibc-bin-2.35-r0.apk glibc-i18n-2.35-r0.apk glibc-2.35-r0.apk 

ENV LC_ALL=C.UTF-8
EXPOSE 8888

WORKDIR /opt

COPY src/main/docker/agent/ flume/plugins.d/agent/
COPY src/main/docker/conf/ flume/conf/
ADD src/main/docker/config/application.yaml daemon/config/application.yaml
ADD src/main/docker/config/log4j2.xml Daemon/config/log4j2.xml

RUN sed -i 's/\r$//' flume/conf/log4j2.xml \
  && sed -i 's/\r$//' flume/conf/flume-env.sh \
  && sed -i 's/\r$//' flume/plugins.d/agent/lib/compile.sh \
  && chmod 755 flume/plugins.d/agent/lib/compile.sh 


COPY target/${JAR_FILE} daemon/app.jar

RUN set -eux; \
    addgroup -g ${G_ID} -S ${USER}; \
    adduser -u ${U_ID} -S -D -G ${USER} -s /bin/sh ${USER};

RUN chown -R ${USER}:${USER} daemon && chown -R ${USER}:${USER} logs && chown -R ${USER}:${USER} flume

WORKDIR /opt/daemon
USER ${USER}
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=dev","-jar","app.jar"]
