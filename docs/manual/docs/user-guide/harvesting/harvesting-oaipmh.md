# OAIPMH Harvesting {#oaipmh_harvester}

This is a harvesting protocol that is widely used among libraries. GeoNetwork implements version 2.0 of the protocol.

## Adding an OAI-PMH harvester

An OAI-PMH server implements a harvesting protocol that GeoNetwork, acting as a client, can use to harvest metadata.

Configuration options:

-   **Site** - Options describing the remote site.
    -   *Name* - This is a short description of the remote site. It will be shown in the harvesting main page as the name for this instance of the OAIPMH harvester.
    -   *URL* - The URL of the OAI-PMH server from which metadata will be harvested.
    -   *Icon* - An icon to assign to harvested metadata. The icon will be used when showing search results.
    -   *Use account* - Account credentials for basic HTTP authentication on the OAIPMH server.
-   **Search criteria** - This allows you to select metadata records for harvest based on certain criteria:
    -   *From* - You can provide a start date here. Any metadata whose last change date is equal to or greater than this date will be harvested. To add or edit a value for this field you need to use the icon alongside the text box. This field is optional so if you don't provide a start date the constraint is dropped. Use the icon to clear the field.
    -   *Until* - Functions in the same way as the *From* parameter but adds an end constraint to the last change date search. Any metadata whose last change data is less than or equal to this data will be harvested.
    -   *Set* - An OAI-PMH server classifies metadata into sets (like categories in GeoNetwork). You can request all metadata records that belong to a set (and any of its subsets) by specifying the name of that set here.
    -   *Prefix* - 'Prefix' means metadata format. The oai_dc prefix must be supported by all OAI-PMH compliant servers.
    -   You can use the Add button to add more than one Search Criteria set. Search Criteria sets can be removed by clicking on the small cross at the top left of the set.

!!! note

    the 'OAI provider sets' drop down next to the *Set* text box and the 'OAI provider prefixes' drop down next to the *Prefix* textbox are initially blank. After specifying the connection URL, you can press the **Retrieve Info** button, which will connect to the remote OAI-PMH server, retrieve all supported sets and prefixes and fill the drop downs with these values. Selecting a value from either of these drop downs will fill the appropriate text box with the selected value.


-   **Options** - Scheduling Options.
-   **Privileges**
-   **Categories**

!!! Notes

    -   if you request the oai_dc output format, GeoNetwork will convert it to Dublin Core format.
    -   when you edit a previously created OAIPMH harvester instance, both the *set* and *prefix* drop down lists will be empty. You have to press the retrieve info button again to connect to the remote server and retrieve set and prefix information.
    -   the id of the remote server must be a UUID. If not, metadata can be harvested but during hierarchical propagation id clashes could corrupt harvested metadata.
