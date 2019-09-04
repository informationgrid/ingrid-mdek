FROM docker-registry.wemove.com/ingrid-java:jdk-8
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-iplug-ige*-installer.jar /

RUN unzip ingrid-iplug-ige*-installer.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-iplug-ige \
    && mkdir -p /opt/ingrid/ingrid-iplug-ige/logs \
    && touch /opt/ingrid/ingrid-iplug-ige/pattern \
    && cp -R /tmp/ingrid-iplug-ige-*/* /opt/ingrid/ingrid-iplug-ige/ \
    && rm -Rf /tmp/* \
    && sed -i -r 's/(<AppenderRef.*\/>)/\1<AppenderRef ref="Console" \/>/g' /opt/ingrid/ingrid-iplug-ige/conf/log4j2.xml \
    && rm ingrid-*.jar

COPY ./files /

#RUN echo jetty.port=80 > /opt/ingrid/ingrid-iplug-ige/conf/config.override.properties
WORKDIR /opt/ingrid/ingrid-iplug-ige
EXPOSE 9923

CMD /bin/sh start.sh start && tail -f /dev/null
