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



## Compilation

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
python ../../../../../closure-library/closure/bin/build/depswriter.py --root_with_prefix="catalog/components ../../components" --root_with_prefix="catalog/js ../../js" --output_file=catalog/lib/closure/deps.js
```

TODO: Add to maven

### Minify

TODO


