# Multilingual editing

A few standards support multilingual metadata (eg. ISO19139, ISO19115-1). A default template is provided for ISO19139 but the user can add translation to an existing record.

To declare a new language in a metadata record:

1. Check the main language defined in the metadata section.
2. Add one or more languages in the other language in the metadata section.
3. Once this is completed, the editor form provides one field per languages declared. There are 2 types of layouts:

    -   One field per language displayed one below the others (eg. default mode for title) to view all available translations
    -   One field per language with the list of language to switch between language.

![](img/multilingual-editing.png)

## Switching layout modes

To switch from one mode to another layout, use the `all` link.

-   When loading the editor, the selected language is the user interface language used if the language is defined in the current record.
-   When viewing the record, if a translation exists in the user interface language, this translation is used; if not, the main metadata language is used.

!!! Note

    This behaviour also applies to multilingual ISO19139 records requested in dublin core from the CSW services.
