#!/usr/bin/env bash

# install less with node first.
# sudo npm install -g less

lessc less/gn_search_sextant.less api.css
sed 's/calc(\(.*\))/~"calc(\1)"/' api.css > less/tmp.css
lessc less/sxt-api.less tmp.css
sed -i 's/.gn .sxt-max-sm/.gn.sxt-max-sm/' tmp.css > api.css
sed 's/.gn .sxt-max-md/.gn.sxt-max-md/' tmp.css > api.css
sed  's/.gn .gn-img-modal/.gn-img-modal/' api.css
rm tmp.css less/tmp.css
