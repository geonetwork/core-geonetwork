# Harvesting an ARCSDE Node {#sde_harvester}

This is a harvesting protocol for metadata stored in an ArcSDE installation.

## Adding an ArcSDE harvester

The harvester identifies the ESRI metadata format: ESRI ISO, ESRI FGDC to apply the required xslts to transform metadata to ISO19139. Configuration options:

-   **Identification**
    -   *Name* - This is a short description of the node. It will be shown in the harvesting main page.
    -   *Group* - User admin of this group and catalog administrator can manage this node.
    -   *Harvester user* - User that owns the harvested metadata.
-   **Schedule** - Schedule configuration to execute the harvester.
-   **Configuration for protocol ArcSDE**
    -   *Server* - ArcSde server IP address or name.
    -   *Port* - ArcSde service port (typically 5151) or ArcSde database port, depending on the connection type selected, see below the *Connection type* section.
    -   *Database name* - ArcSDE instance name (typically esri_sde).
    -   *ArcSde version* - ArcSde version to harvest. The data model used by ArcSde is different depending on the ArcSde version.
    -   *Connection type*
        -   *ArcSde service* - Uses the ArcSde service to retrieve the metadata.

            !!! note

                Additional installation steps are required to use the ArcSDE harvester because it needs proprietary ESRI Java api jars to be installed.
    
                ArcSDE Java API libraries need to be installed by the user in GeoNetwork (folder INSTALL_DIR_GEONETWORK/WEB-INF/lib), as these are proprietary libraries not distributed with GeoNetwork.
    
                The following jars are required:
    
                -   jpe_sdk.jar
                -   jsde_sdk.jar
    
                dummy-api-XXX.jar must be removed from INSTALL_DIR/web/geonetwork/WEB-INF/lib


        -   *Database direct connection* - Uses a database connection (JDBC) to retrieve the metadata. With

            !!! note

                Database direct connection requires to copy JDBC drivers in INSTALL_DIR_GEONETWORK/WEB-INF/lib.


            !!! note

                Postgres JDBC drivers are distributed with GeoNetwork, but not for Oracle or SqlServer.

    -   *Database type* - ArcSde database type: Oracle, Postgres, SqlServer. Only available if connection type is configured to *Database direct connection*.
    -   *Username* - Username to connect to ArcSDE server.
    -   *Password* - Password of the ArcSDE user.
-   **Advanced options for protocol arcsde**
    -   *Validate records before import* - Defines the criteria to reject metadata that is invalid according to XSD and schematron rules.
        -   Accept all metadata without validation.
        -   Accept metadata that are XSD valid.
        -   Accept metadata that are XSD and schematron valid.
-   **Privileges** - Assign privileges to harvested metadata.
