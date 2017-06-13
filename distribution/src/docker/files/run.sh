#!/bin/bash

###
# **************************************************-
# InGrid IGE Distribution
# ==================================================
# Copyright (C) 2014 - 2017 wemove digital solutions GmbH
# ==================================================
# Licensed under the EUPL, Version 1.1 or – as soon they will be
# approved by the European Commission - subsequent versions of the
# EUPL (the "Licence");
# 
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
# http://ec.europa.eu/idabc/eupl5
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and
# limitations under the Licence.
# **************************************************#
###
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
