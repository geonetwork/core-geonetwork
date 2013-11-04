# web-ui module

## Content

This module contains a web user interface for GeoNetwork opensource based on AngularJS, Bootstrap and d3.js librairies.



## Compile

Closure project is used to check, compile and manage JS dependencies.

### Install closure & closure utilities

See https://developers.google.com/closure/compiler/?hl=fr

See https://developers.google.com/closure/utilities/docs/linter_howto for installation


```
cd /path/to/closure-library-parent-dir
git clone http://code.google.com/p/closure-library/
cd closure-library
wget http://closure-compiler.googlecode.com/files/compiler-latest.zip
unzip compiler-latest.zip
```

### Build with maven


Maven compilation take care of running:
 * fixjsstyle for fix JS style
 * gjslint for checking JS files
 * depswriter for building lib/closure.deps.js file containing JS dependency tree
 * closurebuilder for minifying JS files for each module
 * LESS compilation in CSS (mvn lesscss:compile)


```
 mvn clean install -Dclosure.path=/path/to/closure-library
or on windows
 mvn clean install -Dclosure.path=c:/path/to/closure-library
```

### Build with closure utility with command line

#### Build dependencies

JS dependencies needs to be updated when adding a new module.

```
git clone http://code.google.com/p/closure-library/
cd core-geonetwork/web-ui/src/main/resources
python ../../../../../closure-library/closure/bin/build/depswriter.py \
    --root_with_prefix="catalog/components ../../components" \
    --root_with_prefix="catalog/js ../../js" \
    --output_file=catalog/lib/closure/deps.js
```


#### Minify JS

Run closurebuilder to create minified version of each modules:
```
export CLOSURE_LIB=/home/closure-library
cd web-ui/src/main/resources
python $CLOSURE_LIB/closure/bin/build/closurebuilder.py \
  --root=catalog \
  --namespace="gn" \
  --output_mode=compiled \
  --compiler_jar=$CLOSURE_LIB/compiler.jar \
  > catalog/lib/gn.min.js


python $CLOSURE_LIB/closure/bin/build/closurebuilder.py \
  --root=catalog \
  --namespace="gn_admin" \
  --output_mode=compiled \
  --compiler_jar=$CLOSURE_LIB/compiler.jar \
  > catalog/lib/gn_admin.min.js

python $CLOSURE_LIB/closure/bin/build/closurebuilder.py \
  --root=catalog \
  --namespace="gn_login" \
  --output_mode=compiled \
  --compiler_jar=$CLOSURE_LIB/compiler.jar \
  > catalog/lib/gn_login.min.js


```


## Run the application with the UI module

```
cd web
mvn jetty:run -Pui
```
and access http://localhost:8080/geonetwork/
