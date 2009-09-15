Building GeoExt
===============

This directory contains configuration files necessary for building GeoExt
(and Ext JS).  The build configuration is intended for use with the jsbuild
utility included in JSTools (http://projects.opengeo.org/jstools).

Brief instructions
------------------

This build dir contains a Makefile, which can be used to build GeoExt. The
build process requires a bash-like shell, make, sed, find and rsync. All this
should be available on well equipped development *nix and OS X boxes. In
addition, JSTools and csstidy are required. The latter is available as csstidy
package on debian-style linux systems.

    $ sudo aptitude install csstidy

To install JSTools, python-setuptools is required. This is available as
python-setuptools package on debian-style linux systems.

    $ sudo aptitude install python-setuptools
    
Now you can easily install JSTools.

    $ easy_install http://svn.opengeo.org/jstools/trunk/

Change into the core/trunk/build directory (the one containing the readme.txt
file you are reading right now), if you are not already here.

    $ cd geoext/build

From here, you can build the library.

    $ make zip
    
Now you can take the resulting GeoExt.zip file and unpack it on your web
server. The library itself resides in the script folder, the resources folder
contains css files and images.

For more complete instructions on building GeoExt with jsbuild, see the
documentation on the project website:
http://www.geoext.org/trac/geoext/wiki/builds.

The Makefile also contains a target for building Ext JS. This requires Ext JS
to be installed in ../../ext (relative to this build dir). Make sure to
provide a license file matching your Ext JS installation and replace the
provided ext-license.js file with it. To build the library with ext.js in the
script folder, run make with the ext and zip targets.

    $ make ext zip
    
The Makefile has even more targets. Invoke make help to see them all.

    $ make help
