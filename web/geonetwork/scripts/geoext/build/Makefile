# Makefile for GeoExt

VERSION = r$(shell echo '$$Revision: 1158 $$' | tr -d "[:alpha:]$$ :")
LIB_NAME = GeoExt
ZIP_NAME = $(LIB_NAME)-$(VERSION).zip

CSS_DIR = $(LIB_NAME)/resources/css
CSS_IMPORTS = $(shell cat ../resources/css/geoext-all-debug.css | grep '@import' | sed 's/^@import "\(.*\)";/\1/')
ALL_CSS = $(CSS_DIR)/geoext-all.css

.PHONEY: help clean ext examples lib zip dist

help:
	@echo "Please use 'make <target>' where <target> is one or more of"
	@echo "  clean    to clean up after building"
	@echo "  ext      to make a custom Ext JS build. See readme.txt for requirements"
	@echo "  examples to make the examples dir"
	@echo "  lib      to make a standalone dir of lib resources"
	@echo "  zip      to make a zip archive of a standalone lib"
	@echo "  dist     to make a zip archive of a standalone lib with examples"
	@echo
	@echo "Example use:"
	@echo "  make lib"
	@echo "  make lib LIB_NAME=MyLib"
	@echo "  make zip"
	@echo "  make zip ZIP_NAME=MyZip.zip"
	@echo "  make dist VERSION=0.5-rc1"

clean:
	-rm -rf $(LIB_NAME)
	-rm -f $(ZIP_NAME)

ext:
	@echo "Building ext.js."
	mkdir -p $(LIB_NAME)/script
	jsbuild full.cfg -v -o $(LIB_NAME)/script/ -s ext.js

examples:
	mkdir -p $(LIB_NAME)/examples 
	rsync -au --exclude=.svn `pwd`/../examples `pwd`/$(LIB_NAME)
	for file in `find $(LIB_NAME)/examples/ -name "*.html"`; do \
	    sed -i "s/\.\.\/lib\/GeoExt\.js/\.\.\/script\/GeoExt\.js/g" $${file} ; \
	    sed -i "s/geoext-all-debug\.css/geoext-all\.css/g" $${file} ; \
	done ; 	

lib:
	@echo "Building the library."
	mkdir -p $(LIB_NAME)/script $(LIB_NAME)/resources/css
	
	jsbuild full.cfg -v -o $(LIB_NAME)/script/ -s GeoExt.js
	echo "GeoExt.VERSION_NUMBER='$(VERSION)';" >> $(LIB_NAME)/script/GeoExt.js 
	
	rsync -au --exclude=.svn `pwd`/../resources `pwd`/$(LIB_NAME)
	
	for file in `find $(CSS_DIR) -name "*.css"`; do \
	    csstidy $${file} --template=highest $${file} ; \
	done ;
	echo "" > $(ALL_CSS)
	for filename in $(CSS_IMPORTS); do \
	    cat $(CSS_DIR)/$${filename} >> $(ALL_CSS) ; \
	done ;
	-rm $(CSS_DIR)/geoext-all-debug.css

	cp ../license.txt $(LIB_NAME)

zip: lib
	@echo "Archiving the library."
	-rm -f $(ZIP_NAME)
	zip $(ZIP_NAME) -r $(LIB_NAME)

dist: examples zip
