FROM ingrid-java
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-iplug-ige*-installer.jar /

RUN unzip ingrid-iplug-ige*-installer.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-iplug-ige \
    && cp -R /tmp/ingrid-iplug-ige-4.0.3-SNAPSHOT/* /opt/ingrid/ingrid-iplug-ige/ \
    #&& chgrp -R ingrid /opt/ingrid \
    #&& chown -R ingrid /opt/ingrid \
    && rm -Rf /tmp/* \
    && rm ingrid-*.jar

COPY ./files /

WORKDIR /opt/ingrid/ingrid-iplug-ige
EXPOSE 9923

CMD /bin/sh start.sh start && tail -f /dev/null
