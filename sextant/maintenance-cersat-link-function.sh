#!/bin/bash

# Cersat / Link / Update function for OpenDAP, OpenSearch and Broadcast links
# https://gitlab.ifremer.fr/sextant/geonetwork/-/issues/670

SERVER=http://localhost:8080/geonetwork
#SERVER=https://sextant.ifremer.fr/geonetwork
CATALOGUSER=sextant
CATALOGPASS=admin
AUTH=""

rm -f /tmp/cookie;
curl -s -c /tmp/cookie -o /dev/null \
  -X GET  \
  --user $CATALOGUSER:$CATALOGPASS \
  -H "Accept: application/json" \
  "$SERVER/srv/api/me";

export TOKEN=`grep XSRF-TOKEN /tmp/cookie | cut -f 7`;
export JSESSIONID=`grep JSESSIONID /tmp/cookie | cut -f 7`;

curl $AUTH "$SERVER/srv/api/search/records/_search?bucket=s101" \
  -H 'accept: application/json, text/plain, */*' \
  -H 'accept-language: eng' \
  -H "X-XSRF-TOKEN: $TOKEN" \
  -H "Cookie: XSRF-TOKEN=$TOKEN; JSESSIONID=$JSESSIONID" \
  -H 'content-type: application/json;charset=UTF-8' \
  --data-raw '{"from":0,"size":1800,"sort":["uuid"],"query":{"query_string":{"query":"+isHarvested:false +groupPublished:CERSAT"}},"_source":{"includes":["uuid","resourceTitleObject*"]},"track_total_hits":true}' \
  --compressed \
  -o results.json

for hit in $(jq -r '.hits.hits[] | @base64' results.json); do
   _jq() {
     echo "${hit}" | base64 --decode | jq -r "${1}"
    }

  title=$(_jq '._source.resourceTitleObject.default')
  uuid=$(_jq '._id')
  echo "$uuid / $title\n"
  functionXml=""

  read -r -d '' functionXml << EOF
{
"xpath":"/mdb:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/*[cit:protocol/*/text() = 'WWW:OPENDAP' and cit:function/*/@codeListValue = 'browsing']/cit:function/*/@codeListValue",
"value":"<gn_replace>download</gn_replace>"
},{
"xpath":"/mdb:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/*[cit:protocol/*/text() = 'WWW:OPENSEARCH' and cit:function/*/@codeListValue = 'browsing']/cit:function/*/@codeListValue",
"value":"<gn_replace>search</gn_replace>"
},{
"xpath":"/mdb:distributionInfo/*/mrd:transferOptions/*/mrd:onLine/*[cit:protocol/*/text() = 'WWW:BROADCAST' and cit:function/*/@codeListValue = 'browsing']/cit:function/*/@codeListValue",
"value":"<gn_replace>download</gn_replace>"
}
EOF

  echo $functionXml

  curl $AUTH "$SERVER/srv/api/records/batchediting?uuids=$uuid" \
    -X 'PUT' \
    -H 'Accept: application/json, text/plain, */*' \
    -H 'Content-Type: application/json;charset=UTF-8' \
    -H "X-XSRF-TOKEN: $TOKEN" \
    -H "Cookie: XSRF-TOKEN=$TOKEN; JSESSIONID=$JSESSIONID" \
    --data-raw "[$functionXml]" \
    --compressed
done


