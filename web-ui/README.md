# web-ui module

## Content

This module contains a web user interface for GeoNetwork opensource based on AngularJS, Bootstrap and d3.js librairies.

## Compile

Wro4j is is used to compile and manage JS dependencies.


## Check & format file

Maven build is using Prettier to format JS and HTML code.

## Translate client app

1. Generate a transifex token API from https://www.transifex.com/user/settings/api/

   The token is to be saved in ``~/.transifexrc`` .

2. Install transifex ``tx`` client from https://developers.transifex.com/docs/cli

   Recommend installing in ``~`` home folder:

   ```bash
   cd ~
   curl -o- https://raw.githubusercontent.com/transifex/cli/master/install.sh | bash
   ```
   
   You will need to restart your shell after ``tx`` client is added to path.

3. Download translations from transifex:

   ```bash
   cd web-ui
   ./download-from-transifex.sh
   ```

4. Commit the changed files:
   
   ```bash
   git add .
   git commit -m "Transifix update"
   ```
   
   And submit as a pull-request.
