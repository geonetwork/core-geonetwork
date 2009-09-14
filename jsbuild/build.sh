#
# Variables
# 
buildpath="$(cd $(dirname $0); pwd)"
releasepath="${buildpath}/../web/geonetwork/scripts/geo/"
venv="${buildpath}/venv"

#
# Command path definitions
#
python="/usr/bin/python"

(cd ${buildpath};
 if [ ! -d ${venv} ]; then
     echo "creating virtual env and installing jstools..."
     ${python} go-jstools.py ${venv} > /dev/null
     echo "done."
 fi;
 echo "running jsbuild..."
 ${venv}/bin/jsbuild -v -o "${releasepath}" full.cfg
 echo "done.")

echo "built files and resources placed in `cd ${releasepath}; pwd`"

exit 0
