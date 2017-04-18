#!/usr/bin/env bash

# install less with node first.
# sudo npm install -g less

echo "Compiling css for API for all themes"

THEMES_PATH=./views/sextant/less/themes/*

for file in $THEMES_PATH
do
  FILE_NAME=`basename ${file}`
  THEME_NAME=${FILE_NAME%.less}
  CSS_NAME=api-${THEME_NAME}.css
  echo "Compiling theme ${CSS_NAME}"
  ./node_modules/less/bin/lessc ./views/sextant/less/gn_search_sextant.less $CSS_NAME --modify-var="theme-name=$THEME_NAME"
  sed 's/calc(\(.*\))/~"calc(\1)"/' $CSS_NAME > ./views/sextant/less/tmp.css
  ./node_modules/less/bin/lessc ./views/sextant/less/sxt-api.less tmp.css
  sed -i 's/.gn .sxt-max-sm/.gn.sxt-max-sm/' tmp.css
  sed 's/.gn .sxt-max-md/.gn.sxt-max-md/' tmp.css > $CSS_NAME
  sed  's/.gn .gn-img-modal/.gn-img-modal/' $CSS_NAME
  rm tmp.css ./views/sextant/less/tmp.css
  mv $CSS_NAME ./views/sextant/api
done

echo "Done - Compiling css for API for all themes"

#./node_modules/less/bin/lessc ./views/sextant/less/gn_search_sextant.less api.css
