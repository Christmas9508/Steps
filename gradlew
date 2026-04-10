#!/bin/sh
##############################################################################
# Gradle start up script for UN*X
##############################################################################
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME=`cd "${APP_HOME:-./}" && pwd -P` || exit

# Determin JAVA_HOME
if [ -z "$JAVA_HOME" ] ; then
    JAVACMD=java
else
    JAVACMD="$JAVA_HOME/bin/java"
fi

exec "$JAVACMD" \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
