# Importing a new record {#import1}

An editor can import metadata in the catalog file in different formats: XML, MEF or ZIP (see [Metadata Exchange Format (MEF)](../../annexes/mef-format.md)).

## Before you begin

The user should have an `editor` profile to access metadata.

1.  Go to the contribute page and select `Import new records`.

    ![](img/import-record-button.png)

    Using the import new records page, you can:

    -   choose `Upload a file from your computer` and choose one XML or MEF file to load
    -   choose `Copy/Paste` and copy the XML document in the textarea
    -   choose `Import a set of files from a folder on the server` and set the path of the folder in the server

    To import multiple file at a time, use the MEF format or the import from server options.

2.  After you have defined the type of import, configure the other import settings:

    ![](img/import-form.png)

    -   `Type of file`: when uploading or loading file from the server, define the type of file to load. It could be XML for importing XML document or MEF (equivalent to ZIP) for importing MEF format.
    -   `Type of record`:  

        -   Use `Metadata` when loading a normal metadata record
        -   Use `Template` when the loaded metadata record will be used as a template.

    -   `Record identifier processing` determines how to handle potential clashes between the UUID of the record loaded and UUIDs of metadata records already present in the catalog. 3 strategies are available:

        -   `None`: the UUID of the record loaded is left unchanged. If a metadata record with the same UUID is already present in the catalog, an error message is returned.
        -   `Overwrite metadata with same UUID`: any existing metadata record in the catalog having the same UUID as the loaded record will be updated.
        -   `Generate UUID for inserted metadata`: a new UUID is affected to the loaded record.

    -   `Apply XSLT conversion` allows to transform the record loaded using an XSLT stylesheet. A list of predefined transformations is provided. The selected transformation should be compatible with the standard of the loaded record (see [Adding XSLT conversion for import](../workflow/batchupdate-xsl.md#customizing-xslt-conversion)).
    -   `Validate` trigger the validation of the record before loading it. In case of error the record is rejected and an error reported.
    -   `Assign to current catalog` assign the current catalog as origin for the record, in case the MEF file indicate another source.
    -   `Assign to Group` define the group of the loaded record. Only [Workspace Groups](../../administrator-guide/managing-users-and-groups/creating-group.md#1-workspace-group) can be selected.
    -   `Assign to Category` define a local category to assign to the loaded record.

3. Click `import` to trigger the import. After processing, a summary is provided with the following details:

    -   the total count of imported metadata
    -   errors messages
    -   if only one record is imported, a link to that record is provided.
