# Harvesting an ARCSDE Node {#sde_harvester}

This is a harvesting protocol for metadata stored in an ArcSDE installation.

## Adding an ArcSDE harvester

The harvester identifies the ESRI metadata format: ESRI ISO, ESRI FGDC to apply the required xslts to transform metadata to ISO19139

<figure>
<img src="web-harvesting-sde.png" alt="web-harvesting-sde.png" />
<figcaption><em>Adding an ArcSDE harvesting node</em></figcaption>
</figure>

Configuration options:

-   **Site**

    > -   *Name* - This is a short description of the node. It will be shown in the harvesting main page.
    > -   *Server* - ArcSde server IP address or name
    > -   *Port* - ArcSde service port (typically 5151)
    > -   *Username* - Username to connect to ArcSDE server
    > -   *Password* - Password of the ArcSDE user
    > -   *Database name* - ArcSDE instance name (typically esri_sde)

-   **Options**

```{=html}
<!-- -->
```
-   **Harvested Content**
    -   *Validate* - if checked then harvested metadata records will be validated against the relevant metadata schema. Invalid records will be rejected.
-   **Privileges**

```{=html}
<!-- -->
```
-   **Categories**
