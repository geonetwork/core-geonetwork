# web-ui module

## Content

This module contains a web user interface for GeoNetwork opensource based on AngularJS, Bootstrap and d3.js librairies.



## Compile

Wro4j is is used to compile and manage JS dependencies.


## Check & format file

Maven build is using Prettier to format JS and HTML code.

## Translate client app

Generate a transifex token API from https://www.transifex.com/user/settings/api/

Install transifex client:

```shell script
curl -o- https://raw.githubusercontent.com/transifex/cli/master/install.sh | bash
```

Download translations from transifex:

```shell script
cd web-ui
./download-from-transifex.sh
```
