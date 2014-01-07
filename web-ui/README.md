# web-ui module

## Content

This module contains a web user interface for GeoNetwork opensource based on AngularJS, Bootstrap and d3.js librairies.



## Compile

Wro4j is is used to compile and manage JS dependencies.


## Check & format file

Closure utility could be used to check syntax and fix style

See https://developers.google.com/closure/utilities/docs/linter_howto for installation

```
sudo easy_install http://closure-linter.googlecode.com/files/closure_linter-latest.tar.gz
```

Maven build running with profile "jslint" runs:
 * fixjsstyle for fix JS style
 * gjslint for checking JS files


```
 mvn clean install -Pjslint
```

