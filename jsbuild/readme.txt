== JS build ==
Some javascript files are minified and concatenated scripts from !JavaScript libraries using [http://projects.opengeo.org/jstools/wiki/jsbuild jsbuild] process. Actually only OpenLayers, GeoExt and extent map editor is using this process. The file full.cfg defines:
 * name of the lib to buld
 * directories to look into


In order to build:
 * remove old lib file.
 * install jsbuid (if not available)
{{{
sudo apt-get install python-setuptools
sudo easy_install jstools
}}}
 * jsbuild directory, run jsbuild utility. This will create gn.libs.js files used by GeoNetwork
{{{
jsbuild -o ../web/geonetwork/scripts/geo full.cfg
}}}
 
If you don't want to use this small file (for debugging), add the "debug" parameter to your url (eg. main.home?debug) which will use the non minified JS files (see xsl/geo/utils.xsl).

