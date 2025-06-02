# SFTP server Harvesting {#sftpserver_harvester}

This harvester will harvest metadata as XML files from a SFTP server.

## Adding a SFTP server harvester

To create a SFTP server harvester go to `Admin console` > `Harvesting` and select `Harvest from` > `SFTP server`:

![](img/add-sftp-harvester.png)

Providing the following information:

-   **Identification**
    -   *Node name and logo*: A unique name for the harvester and, optionally, a logo to assign to the harvester.
    -   *Group*: Group which owns the harvested records. Only the catalog administrator or users with the profile `UserAdmin` of this group can manage the harvester.
    -   *User*: User who owns the harvested records.

-   **Schedule**: Scheduling options to execute the harvester. If disabled, the harvester must be run manually from the harvester page. If enabled, a scheduling expression using cron syntax should be configured ([See examples](https://www.quartz-scheduler.org/documentation/quartz-2.1.7/tutorials/crontrigger)).

-   **Configure connection to SFTP**
    -   *Server*: The SFTP host name or IP address.
    -   *SFTP port*: The port to connect to (usually 22).
    -   *Remote folder*: The path name of the directory containing the metadata (as XML files) to be harvested. The directory must be accessible by GeoNetwork.
    -   *Also search in subfolders*: If checked and the *Directory* path contains other directories, then the harvester will traverse the entire file system tree in that directory and add all metadata files found.
    -   *Username*: The username to connect to the SFTP server.
    -   *Use private  public keys*: Generates a private/public key to connect to the SFTP server instead of using a password. The user must configure the public key on the SFTP server.
    -   *Key type*: Select the algorithm to create the keys: RSA (4096 bits) / ECDSA.
    -   *Password*: The password to connect to the SFTP server. Only applies when *Use private  public keys* is not enabled.
    
-   **Configure response processing for filesystem**
    -   *Action on UUID collision*: When a harvester finds the same uuid on a record collected by another method (another harvester, importer, dashboard editor,...), should this record be skipped (default), overriden or generate a new UUID?
    -   *Update catalog record only if file was updated*
    -   *Keep local even if deleted at source*: If checked then metadata records that have already been harvested will be kept even if they have been deleted from the *Directory* specified.
    -   *Validate records before import*: Defines the criteria to reject metadata that is invalid according to XML structure (XSD) and validation rules (schematron).
        -   Accept all metadata without validation.
        -   Accept metadata that are XSD valid.
        -   Accept metadata that are XSD and schematron valid.
    -   *XSL transformation to apply*: (Optional)  The referenced XSL transform will be applied to each metadata record before it is added to GeoNetwork.
    -   *Batch edits*: (Optional) Allows to update harvested records, using XPATH syntax. It can be used to add, replace or delete element.
    -   *Category*: (Optional) A GeoNetwork category to assign to each metadata record.

-   **Privileges** - Assign privileges to harvested metadata.
