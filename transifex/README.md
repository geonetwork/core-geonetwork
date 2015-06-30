The module is a maven plugin that is capable of transforming the geonetwork XML files (as well has handling the JSON translation files) 
into the correct transifex format.

This plugin also uploads the english files for translation and downloads the translations and converts them back into the correct XML or JSON format.

Typically the plugin won't execute because it isn't configured with a with a username or password.  In order to execute it
 
    mvn install -Dtransifex-username=<username> -Dtransifex-password=<password> 