#!/bin/sh

#set -x

HOST=$1
CHECK=$2
USERNAME=$3
PASSWORD=$4

OUT=/tmp/$(echo $CHECK|sed 's|/|_|g')


if [ "$HOST" == "" ] || [ "$CHECK" == "" ] || [ "$USERNAME" == "" ] || [ "$PASSWORD" == "" ] ; then
    echo "Usage:   healthcheck.sh <protocol://host:port> <urlpath> <username> <password>"
    echo "Example: healthcheck.sh http://localhost:8080 geonetwork/criticalhealthcheck monitor monitor"
    exit 1
fi

curl -s -c /tmp/cookie "$HOST/geonetwork/srv/eng/user.login?username=$USERNAME&password=$PASSWORD" -o /dev/null

CODE=`curl -sL --cookie /tmp/cookie -w "%{http_code}\\n" "$HOST/$CHECK" -o $OUT`

if [ "$CODE" == "200" ]; then 
    exit 0
else 
    exec<$OUT
    FAILURE=""
    while read line; do
        if grep -v -q ": OK" <<<$line ; then
            FAILURE="$FAILURE || $line";
        fi
    done
fi