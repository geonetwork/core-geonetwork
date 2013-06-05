- *File Type* - First option is to choose the type of metadata record you are loading. The two choices are:

 - *Metadata* - use when loading a normal metadata record 
 - *Template* - use when loading a metadata record that will be used as a template to build new records in the editor.

- *Import Action* - This option group determines how to handle potential clashes between the UUID of the metadata record you are loading and the UUIDs of metadata records already present in the catalog. There are three actions and you can select one:

 - *No action on import* - the UUID of the metadata record you are loading is left unchanged. If a metadata record with the same UUID is already present in the catalog, you will receive an error message.
 - *Overwrite metadata with same UUID* - any existing metadata record in the catalog with the same UUID as the record you are loading will be replaced with the metadata record you are loading.
 - *Generate UUID for inserted metadata* - create new a UUID for the metadata records you are loading.

- *Stylesheet* - Allows you to transform the metadata record using an XSLT stylesheet before loading the record. The drop down control is filled with the names of files taken from the *INSTALL_DIR/web/geonetwork/xsl/conversion/import folder*. (Files can be added to this folder without restarting GeoNetwork). As an example, you could use this option to convert a metadata into schema that is supported by GeoNetwork. 

- *Validate* - The metadata is validated against its schema before loading. If it is not valid it will not be loaded. 

- *Group* - Use this option to select a user group to assign to the imported metadata. 

- *Category* -  Use this option to select a local category to assign to the imported metadata. Categories are local to the catalogue you are using and are intended to provide a simple way of searching groups of metadata records.

