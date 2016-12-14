#!/bin/bash

set -euo pipefail
IFS=$'\n\t'

FQDN=$(doguctl config --global fqdn)
export SMEAGOL_SERVICE_URL="https://${FQDN}/smeagol"
export SMEAGOL_CAS_URL="https://${FQDN}/cas"
export SCM_INSTANCE_URL="https://${FQDN}/scm"

TRUSTSTORE="${SMEAGOL_HOME}/truststore.jks"
create_truststore.sh "${TRUSTSTORE}" > /dev/null

java -Djava.awt.headless=true \
  -Dfile.encoding=UTF-8 \
  -Djava.net.preferIPv4Stack=true \
  -Djavax.net.ssl.trustStore="${TRUSTSTORE}" \
  -Djavax.net.ssl.trustStorePassword=changeit \
  -jar /app/smeagol-app.jar
