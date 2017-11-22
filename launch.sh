#!/bin/bash
"${HOST?Please provide host IP}"

[ $(git rev-parse HEAD) = $(git ls-remote $(git rev-parse --abbrev-ref @{u} | \
sed 's/\// /g') | cut -f1) ] && echo up to date || (echo not up to date; git pull; ./gradlew shadowJar)


while :
do
    cd /root/vandyinnovation.com
    java -Dvertx.host="$HOST" -Dvertx.port="443" -Dvertx.key="/root/certs/vandyinnovation.key" -Dvertx.cert="/root/certs/origin.crt" -Dvertx.ssl="true" -Dvertx.origin="/root/certs/origin-pull-ca.pem" -jar build/libs/vandyinnovation.com-1.0-SNAPSHOT-fat.jar
    sleep 3
done
