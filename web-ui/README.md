# web-ui module

## Content

This module contains a web user interface for GeoNetwork opensource based on AngularJS, Bootstrap and d3.js librairies.

Currently supported features:
 * Authentification
  * User authentification
 * Administration
  * System information
  * Users & groups management
  * Search and catalog content statistics
  * Catalog status



## Compile

Closure project is used to check, compile and manage JS dependencies.

### Install closure

See https://developers.google.com/closure/compiler/?hl=fr

### Style & linter

Maven compilation take care of running:
 * fixjsstyle for fix JS style
 * gjslint for checking JS files

Closure utilities needs to be installed. See https://developers.google.com/closure/utilities/docs/linter_howto for installation


### Dependency
JS dependencies needs to be updated when adding a new module.

```
git clone http://code.google.com/p/closure-library/
cd core-geonetwork/web-ui/src/main/resources
python ../../../../../closure-library/closure/bin/build/depswriter.py \
    --root_with_prefix="catalog/components ../../components" \
    --root_with_prefix="catalog/js ../../js" \
    --output_file=catalog/lib/closure/deps.js
```

TODO: Add to maven

### Minify


Download compiler from http://closure-compiler.googlecode.com/files/compiler-latest.zip

See https://developers.google.com/closure/library/docs/closurebuilder?hl=fr


Run closurebuilder to create minified version of each modules:
```
python ../../../../../closure-library/closure/bin/build/closurebuilder.py \
  --root=catalog \
  --namespace="gn" \
  --output_mode=compiled \
  --compiler_jar=../../../../compiler.jar \
  > catalog/lib/gn.min.js


python ../../../../../closure-library/closure/bin/build/closurebuilder.py \
  --root=catalog \
  --namespace="gn_admin" \
  --output_mode=compiled \
  --compiler_jar=../../../../compiler.jar \
  > catalog/lib/gn_admin.min.js

python ../../../../../closure-library/closure/bin/build/closurebuilder.py \
  --root=catalog \
  --namespace="gn_login" \
  --output_mode=compiled \
  --compiler_jar=../../../../compiler.jar \
  > catalog/lib/gn_login.min.js

```

TODO: Add to maven
 * May be an option https://code.google.com/p/wro4j/wiki/MavenPlugin?ts=1284124553&updated=MavenPlugin

### Compile

```
mvn clean install
```

## Run

```
cd web
mvn jetty:run -Pui
```
and access http://localhost:8080/geonetwork/catalog/
