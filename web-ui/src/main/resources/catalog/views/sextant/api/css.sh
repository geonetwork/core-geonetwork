#!/usr/bin/env bash

# install less with node first.
# sudo npm install -g less

echo "Compiling CSS for API themes..."

THEMES_PATH=./views/sextant/less/themes/*

for file in $THEMES_PATH
do
  FILE_NAME=`basename ${file}`
  THEME_NAME=${FILE_NAME%.less}
  CSS_NAME=api-${THEME_NAME}.css
  echo "> Compiling theme ${CSS_NAME}"

  # compile base Sextant style, store in tmp.css
  ./node_modules/less/bin/lessc ./views/sextant/less/gn_search_sextant.less tmp.css --modify-var="theme-name=$THEME_NAME"
  sed -i 's/calc(\(.*\))/~"calc(\1)"/' tmp.css
  mv tmp.css ./views/sextant/less

  # compile Sextant API-specific style, including previously generated tmp.css
  ./node_modules/less/bin/lessc ./views/sextant/less/sxt-api.less $CSS_NAME --modify-var="theme-name=$THEME_NAME"
  sed -i 's/.gn .sxt-max-sm/.gn.sxt-max-sm/' $CSS_NAME
  sed -i 's/.gn .sxt-max-md/.gn.sxt-max-md/' $CSS_NAME
  sed -i 's/.gn .gn-img-modal/.gn-img-modal/' $CSS_NAME
  mv $CSS_NAME ./views/sextant/api

  # cleanup
  rm ./views/sextant/less/tmp.css
done

echo "Compiling CSS for API themes - Done"

#./node_modules/less/bin/lessc ./views/sextant/less/gn_search_sextant.less api.css
