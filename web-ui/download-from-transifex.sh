#!/usr/bin/env bash
# Install transifex client first.
# sudo apt-get install transifex-client
# on OSX run pip if you have that installed
# sudo pip install transifex-client

mkdir transifex-src
cd transifex-src
tx init --host=www.transifex.com
tx set --auto-remote https://www.transifex.com/projects/p/core-geonetwork/
tx pull -a -r 'core-geonetwork.editor'
tx pull -a -r 'core-geonetwork.admin'
tx pull -a -r 'core-geonetwork.core'
tx pull -a -r 'core-geonetwork.v4'
tx pull -a -r 'core-geonetwork.search'
tx pull -a -r 'core-geonetwork.gnui'
cd ..

TRANSLATION_DIR=transifex-src/translations/core-geonetwork.
SRC_DIR=src/main/resources/catalog/locales

l=(
    'es::es'
    'fr::fr'
    'de::de'
    'it::it'
    'ko::ko'
    'nl::nl'
    'cs::cs_CZ'
    'ca::ca'
    'pt::pt_BR'
    'fi::fi'
    'is::is'
    'ru::ru'
    'zh::zh',
    'sk::sk_SK',
    'sv::sv_SE'
)

for index in "${l[@]}" ; do
    KEY="${index%%::*}"
    VALUE="${index##*::}"
  echo "Language $VALUE"
  for mod in admin core editor search v4 gnui
  do
    cp $TRANSLATION_DIR$mod/$VALUE.json $SRC_DIR/$KEY-$mod.json
  done;
done

# Reference file for GeoNetwork-UI project served by the i18n API
wget -O $SRC_DIR/en-gnui.json https://raw.githubusercontent.com/geonetwork/geonetwork-ui/master/apps/search/src/assets/i18n/en.json


