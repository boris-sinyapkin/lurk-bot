#!/bin/bash -e

exec ${JAVA_HOME}/bin/java \
    "$@" \
    -jar "$(dirname $0)"/lurkbot-1.0-SNAPSHOT-jar-with-dependencies.jar