#!/bin/bash
PROFILES_DIR="/opt/ingrid/ingrid-iplug-ige/profiles"

if [ "$PROFILE" ]; then
    echo "Using specific profile: $PROFILE"

    if [ ! -d "$PROFILES_DIR/$PROFILE" ]; then
        echo >&2 "PROFILE DIRECTORY NOT FOUND: '$PROFILES_DIR/$PROFILE'"
        exit 1
    fi

    echo "Copying profile files ..."
    cp -R $PROFILES_DIR/$PROFILE/* /opt/ingrid/ingrid-iplug-ige

else
    echo "No specific profile used."
fi

if [ "$DEBUG" = 'true' ]; then
    INGRID_OPTS="$INGRID_OPTS -agentlib:jdwp=transport=dt_socket,address=7100,server=y,suspend=n"
fi

cd /opt/ingrid/ingrid-iplug-ige
sh start.sh start