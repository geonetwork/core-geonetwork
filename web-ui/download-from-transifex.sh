#!/usr/bin/env bash
# Install transifex client first.
# sudo apt-get install transifex-client

mkdir transifex-src
cd transifex-src
tx init --host=www.transifex.com
tx set --auto-remote https://www.transifex.com/projects/p/core-geonetwork/
tx pull -a
cd ..

TRANSLATION_DIR=transifex-src/translations/core-geonetwork.
SRC_DIR=src/main/resources/catalog/locales

declare -A l
l[es]="es"
l[fr]="fr"
l[ge]="de"
l[it]="it"
l[ko]="ko"
l[du]="nl"
l[cz]="cs_CZ"


for lang in du es fr ge it ko cz
do
  echo "Language $lang"
  for mod in admin core editor search
  do 
    cp $TRANSLATION_DIR$mod/${l[$lang]}.json $SRC_DIR/$lang-$mod.json
  done;
done;


