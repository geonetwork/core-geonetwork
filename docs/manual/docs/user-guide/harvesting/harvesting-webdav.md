# WEBDAV Harvesting {#webdav_harvester}

This harvesting type uses the WebDAV (Distributed Authoring and Versioning) protocol or the WAF (web accessible folder) protocol to harvest metadata from a web server. It can be useful to users that want to publish their metadata through a web server that offers a DAV interface. The protocol permits retrieval of the contents of a web page (a list of files) along with the change date.

## Adding a WebDAV harvester

-   **Site** - Options about the remote site.
    -   *Subtype* - Select WebDAV or WAF according to the type of server being harvested.
    -   *Name* - This is a short description of the remote site. It will be shown in the harvesting main page as the name for this instance of the WebDAV harvester.
    -   *URL* - The remote URL from which metadata will be harvested. Each file found that ends with .xml is assumed to be a metadata record.
    -   *Icon* - An icon to assign to harvested metadata. The icon will be used when showing search results.
    -   *Use account* - Account credentials for basic HTTP authentication on the WebDAV/WAF server.
-   **Options** - Scheduling options.
-   **Options** - Specific harvesting options for this harvester.
    -   *Validate* - If checked, the metadata will be validated after retrieval. If the validation does not pass, the metadata will be skipped.
    -   *Recurse* - When the harvesting engine will find folders, it will recursively descend into them.
-   **Privileges** - Assign privileges to harvested metadata.
-   **Categories**

!!! Notes

    -   The same metadata could be harvested several times by different instances of the WebDAV harvester. This is not good practise because copies of the same metadata record will have a different UUID.
