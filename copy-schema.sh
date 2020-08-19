current=`pwd`
echo "Copy schemas in $current"
cd schemas
for d in *; do
 if [ -d "$d/src/main/plugin/$d" ]; then
   echo $d
   rm -fr ../web/src/main/webapp/WEB-INF/data/config/schema_plugins/$d
   cp -fr $d/src/main/plugin/$d ../web/src/main/webapp/WEB-INF/data/config/schema_plugins/.
 fi
done

