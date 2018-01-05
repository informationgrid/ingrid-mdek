FROM docker-registry.wemove.com/ingrid-java:jdk-8
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-iplug-ige*-installer.jar /

RUN unzip ingrid-iplug-ige*-installer.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-iplug-ige \
    && touch /opt/ingrid/ingrid-iplug-ige/pattern \
    && cp -R /tmp/ingrid-iplug-ige-*/* /opt/ingrid/ingrid-iplug-ige/ \
    && rm -Rf /tmp/* \
    && rm ingrid-*.jar

COPY ./files /

WORKDIR /opt/ingrid/ingrid-iplug-ige
EXPOSE 9923

CMD /bin/sh start.sh start && tail -f /dev/null
