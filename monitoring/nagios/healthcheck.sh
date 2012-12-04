#!/bin/bash

#set -x

STATE_OK=0
STATE_WARNING=1
STATE_CRITICAL=2
STATE_UNKNOWN=3
STATE_DEPENDENT=4

HOST=$1
CHECK=$2
USERNAME=$3
PASSWORD=$4

OUT=/tmp/$(echo $CHECK|sed 's|/|_|g')
COOKIE_FILE=/tmp/curl_cookies

if [ "x$HOST" = "x" ] || [ "x$CHECK" = "x" ] || [ "x$USERNAME" = "x" ] || [ "x$PASSWORD" = "x" ] ; then
    echo "Usage:   healthcheck.sh <protocol://host:port> <urlpath> <username> <password>"
    echo "Example: healthcheck.sh http://localhost:8080 geonetwork/criticalhealthcheck monitor monitor"
    exit 1
fi

curl -s -c $COOKIE_FILE "$HOST/geonetwork/srv/eng/user.login?username=$USERNAME&password=$PASSWORD" -o /dev/null

CODE=`curl -sL --cookie $COOKIE_FILE -w "%{http_code}\\n" "$HOST/$CHECK" -o $OUT`
rm -f $COOKIE_FILE


RESPONSE="Health checks pass"
EXIT=$STATE_OK
STATUS="OK"
if [ "x$CODE" != "x200" ]; then
    # Add line to file so that last line is processed by while loop
    echo "" >> $OUT

    FAILURE=""
    while read line; do
	HASFAILURE=`grep -v -q ": OK" <<< $line`

        if [ "x$HASFAILURE" = "x" ] ; then
            FAILURE="$FAILURE || $line";
        fi
    done < $OUT
    
    ISWARNING=`grep -q "/warninghealthcheck" <<< $CHECK`
    if [ "x$ISWARNING" != "x" ]; then
        STATUS="WARNING"
        EXIT=$STATE_WARNING
    else
        STATUS="CRITICAL"
        EXIT=$STATE_CRITICAL
    fi
    RESPONSE=$FAILURE
fi

echo "$STATUS: $RESPONSE"
exit $EXIT