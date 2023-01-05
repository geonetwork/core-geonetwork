#!/bin/bash

DISTRIBUTIONS=(
bullseye
)

COMPONENTS=(
dev
)

if [ $# -eq 0 ]; then
    echo "Usage: ./update-repos.sh [GPG_KEY_ID]"
    exit 1
fi

TIMESTAMP=$(date +%s)
APTLY_CONF=/mnt/aptly.conf

for distrib in ${DISTRIBUTIONS[@]}; do
	for component in ${COMPONENTS[@]}; do
		aptly --config=$APTLY_CONF repo create -distribution=$distrib -component=$component sig-$distrib-$component
		aptly --config=$APTLY_CONF repo add sig-$distrib-$component /mnt/$distrib/$component/*.deb
	done
	aptly --config=$APTLY_CONF publish repo -gpg-key="$1" -component=`IFS=$','; echo "${COMPONENTS[*]}"` `for component in ${COMPONENTS[@]}; do echo -n "sig-$distrib-$component "; done`
	aptly --config=$APTLY_CONF publish update -gpg-key="$1" /mnt/$distrib

	for component in ${COMPONENTS[@]}; do
		aptly --config=$APTLY_CONF snapshot create sig-$distrib-$component-$TIMESTAMP from repo sig-$distrib-$component
	done

	aptly --config=$APTLY_CONF publish drop $distrib filesystem:httpd:
	aptly --config=$APTLY_CONF publish snapshot -gpg-key="$1" -component=`IFS=$','; echo "${COMPONENTS[*]}"` `for component in ${COMPONENTS[@]}; do echo -n "sig-$distrib-$component-$TIMESTAMP "; done` filesystem:httpd:
done

