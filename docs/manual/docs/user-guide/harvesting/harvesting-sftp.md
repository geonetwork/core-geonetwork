# SFTP Server Harvesting {#sftpserver_harvester}

This harvester collects metadata as XML files from a SFTP server.

## Adding a SFTP Server Harvester

To create a SFTP server harvester, navigate to `Admin console` > `Harvesting` and select `Harvest from` > `SFTP server`:

![](img/add-sftp-harvester.png)

Provide the following information:

- **Identification**
    - *Node name and logo*: Specify a unique name for the harvester and, optionally, assign a logo.
    - *Group*: Select the group that will own the harvested records. Only the catalog administrator or users with the `UserAdmin` profile for this group can manage the harvester.
    - *User*: Indicate the user who will own the harvested records.

- **Schedule**: Configure scheduling options for executing the harvester. If disabled, the harvester must be run manually from the harvester page. If enabled, provide a scheduling expression using cron syntax.

- **SFTP Connection Configuration**
    - *SFTP host address*: Enter the SFTP host name or IP address (omit the 'sftp://' prefix).
    - *SFTP port*: Specify the port to connect to (usually 22).
    - *SFTP home directory*: Enter the directory path on the SFTP server containing the metadata (XML files) to be harvested. Use '/' to start from the root directory of the connection.
    - *Recurse subfolders*: If checked, the harvester will recursively process metadata in all folders within the specified home directory, including subfolders. If unchecked, only items in the specified directory will be harvested.
    - *Username*: Provide the username for connecting to the SFTP server.
    - *Use SSH key*: If checked, a private/public key pair will be generated to connect to the SFTP server. Using an SSH key is a more secure authentication method than a password and is recommended.
        - *SSH key type*: Choose the algorithm for SSH key creation: RSA (4096 bits) or ECDSA.
    - *Password*: Enter the password for connecting to the SFTP server. This is only required if *Use SSH key* is unchecked.

- **Response Processing Configuration**
    - *Action on UUID collision*: Specify what to do when a harvested record shares a UUID with a record collected by another method (e.g., another harvester, importer, dashboard editor). You can choose to skip the record (default) or update it.
    - *Update catalog record only if file was updated*: Only update the catalog record if the source file has changed.
    - *Keep local even if deleted at source*: If checked, metadata records that have already been harvested will be retained even if they are deleted from the specified directory on the SFTP server.
    - *Validate records before import*: Set the criteria for rejecting invalid metadata based on XML structure (XSD) and validation rules (schematron):
        - Accept all metadata without validation.
        - Accept metadata that are XSD valid.
        - Accept metadata that are both XSD and schematron valid.
    - *XSL transformation to apply*: (Optional) Reference an XSL transform to apply to each metadata record before it is added to GeoNetwork.
    - *Batch edits*: (Optional) Update harvested records using XPath syntax. You can add, replace, or delete elements.
    - *Category*: (Optional) Assign a GeoNetwork category to each metadata record.

- **Privileges**: Assign privileges to the harvested metadata.
