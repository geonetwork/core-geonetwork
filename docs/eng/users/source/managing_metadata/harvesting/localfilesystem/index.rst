.. _localfilesystem_harvester:

Local File System Harvesting
----------------------------

This harvester will harvest metadata as XML files from a filesystem available on the machine running the GeoNetwork server.

Adding a Local File System harvester
````````````````````````````````````

.. figure:: web-harvesting-localfilesystem.png

    *Adding a Local Filesystem harvester*

The figure above shows the options available:

- **Site** - Options about the remote site.

    - *Name* - This is a short description of the filesystem harvester. It will be shown in the harvesting main page as the name for this instance of the Local Filesystem harvester.
    - *Directory* - The path name of the directory containing the metadata (as XML files) to be harvested.
    - *Recurse* - If checked and the *Directory* path contains other directories, then the harvester will traverse the entire file system tree in that directory and add all metadata files found.
    - *Keep local if deleted at source* - If checked then metadata records that have already been harvested will be kept even if they have been deleted from the *Directory* specified.
    - *Icon* - An icon to assign to harvested metadata. The icon will be used when showing harvested metadata records in the search results. 

- **Options** - Scheduling options.

.. include:: ../common_options.rst

- **Harvested Content** - Options that are applied to harvested content.

  - *Apply this XSLT to harvested records* - Choose an XSLT here that will convert harvested records to a different format.
  - *Validate* - If checked, the metadata will be validated after retrieval. If the validation does not pass, the metadata will be skipped. 

- **Privileges** - Assign privileges to harvested metadata. 

.. include:: ../common_privileges.rst

- **Categories** 

.. include:: ../common_categories.rst

Notes
`````
 - in order to be successfully harvested, metadata records retrieved from the file system must match a metadata schema in the local GeoNetwork instance
