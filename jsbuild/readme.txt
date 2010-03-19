== JS build ==
Some javascript files are minified and concatenated scripts 
from !JavaScript libraries using [http://projects.opengeo.org/jstools/wiki/jsbuild jsbuild] 
process. 

The file full.cfg defines:
 * name of the lib to build
 * directories to look into

In order to build:
 * remove old lib file in web/geonetwork/scripts/lib/*.
 * install jsbuid (if not available)
  * Linux users:
{{{
sudo apt-get install python-setuptools
sudo easy_install jstools
}}}
  * Window users:
   * Install python (i.e 2.6.x)
   * Install setuptools from http://pypi.python.org/pypi/setuptools
   * Downlaod JSTools from http://pypi.python.org/simple/JSTools/
   * Run in command line : C:\path_To_Python\Scripts\easy_install C:\Path_to_jstools\
   * Change directory to C:\geonetwork_installation\jsbuild and then launch C:\path_To_Python\Scripts\jsbuild.exe -o ..\web\geonetwork\scripts\lib full.cfg
 * in jsbuild directory, run jsbuild utility. This will create Javascript files used by GeoNetwork
  * on Linux
   * jsbuild -o ../web/geonetwork/scripts/lib full.cfg
   * ./build.sh
  * or on windows 
   * C:\path_To_Python\Scripts\jsbuild.exe -o ..\web\geonetwork\scripts\lib full.cfg
  
If you don't want to use this small file (for debugging), add the "debug" parameter
 to your url (eg. main.home?debug) which will use the non minified JS files.

