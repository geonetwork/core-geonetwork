#!/usr/bin/env bash
# Install transifex client first.
# curl -o- https://raw.githubusercontent.com/transifex/cli/master/install.sh | bash
# source ~/.bashrc

rm -fr transifex-src
mkdir -p transifex-src/.tx
cd transifex-src

cat <<EOF > .tx/config
  [main]
  host = https://www.transifex.com

  [o:geonetwork:p:core-geonetwork:r:v4]
  file_filter = translations/core-geonetwork.v4/<lang>.json
  source_file = translations/core-geonetwork.v4/en.json
  type = KEYVALUEJSON

  [o:geonetwork:p:core-geonetwork:r:editor]
  file_filter = translations/core-geonetwork.editor/<lang>.json
  source_file = translations/core-geonetwork.editor/en.json
  type = KEYVALUEJSON

  [o:geonetwork:p:core-geonetwork:r:admin]
  file_filter = translations/core-geonetwork.admin/<lang>.json
  source_file = translations/core-geonetwork.admin/en.json
  type = KEYVALUEJSON

  [o:geonetwork:p:core-geonetwork:r:core]
  file_filter = translations/core-geonetwork.core/<lang>.json
  source_file = translations/core-geonetwork.core/en.json
  type = KEYVALUEJSON

  [o:geonetwork:p:core-geonetwork:r:search]
  file_filter = translations/core-geonetwork.search/<lang>.json
  source_file = translations/core-geonetwork.search/en.json
  type = KEYVALUEJSON

  [o:geonetwork:p:core-geonetwork:r:gnui]
  file_filter = translations/core-geonetwork.gnui/<lang>.json
  source_file = translations/core-geonetwork.gnui/en.json
  type = KEYVALUEJSON
EOF


tx pull --force --translations --all
cd ..

TRANSLATION_DIR=transifex-src/translations/core-geonetwork.
SRC_DIR=src/main/resources/catalog/locales


l=(
    'az::az'
    'hy::hy'
    'ca::ca'
    'cs::cs_CZ'
    'cy::cy'
    'da::da_DK'
    'de::de'
    'es::es'
    'fi::fi'
    'fr::fr'
    'is::is'
    'it::it'
    'ka::ka'
    'ko::ko'
    'nl::nl'
    'pt::pt_BR'
    'ro::ro'
    'ru::ru'
    'sk::sk_SK'
    'sv::sv_SE'
    'uk::uk'
    'zh::zh'
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


