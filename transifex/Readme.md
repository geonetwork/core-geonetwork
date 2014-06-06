The binaries in this folder are from the https://github.com/jesseeichar/golang-transifex project.  

Upload
------

Upload all the 'source language' (default english) files defined in the ../transifex-localization-files.json configuration file.
Optionally also upload translations.

Transifex has the concept of "resources".  Each resource represents a set of translation files for all languages.  There is a source language which contains all strings that need to be translated.  This file is not editable.  There are also any number of "translations" for a "resource".  Each translation are all strings in a particular language that have been translated.

Running the upload command will:

1. Create all resources in the configuration file that are not currently in transifex
2. Upload the contents of the 'source language' translations file.
3. If a resource was created all translations will be uploaded


Download
--------

All translations will be downloaded and written to the appropriate files.  This will overwrite the previous files on disk without warning but if there is a problem git can be used to roll back the changes.