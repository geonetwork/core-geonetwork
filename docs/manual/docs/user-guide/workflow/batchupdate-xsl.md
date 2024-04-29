# Update records using XSLT {#batchupdate_xsl}

## Applying changes {#batch-process-apply}

Administrator user can use the catalog API to update a set of records with an XSLT transformation. The steps are the following:

-   Sign in as admin user
-   Search records to be updated using the `q` service eg. select all templates in a specific standard: <http://localhost:8080/geonetwork/srv/eng/q?_schema=iso19115-3&_isTemplate=y>
-   Select all records matching the search with <http://localhost:8080/geonetwork/srv/eng/metadata.select?selected=add-all>
-   Trigger the process with <http://localhost:8080/geonetwork/srv/eng/md.processing.batch?process=my-custom-process>
-   Check the progress report with <http://localhost:8080/geonetwork/srv/eng/md.processing.batch.report>

At the end of the process, a summary describes:

-   Number of records to process
-   Number of records to processed
-   Number of records not found (in case one record gets deleted since the selection)
-   Number of records with errors
-   Number of records with process not defined in its standard
-   Number of records that current user can't edit

A process can also be applied to one metadata record using the `md.processing` service. eg. <http://localhost:8080/geonetwork/srv/eng/md.processing?uuid=46eac9e4-33cb-45b7-a104-7bcc8e654c98&process=keywords-mapper&search=water&replace=Water>

Parameters are:

-   uuid or id: the metadata UUID or the metadata internal identifier
-   process: The process identifier
-   add other extra parameters depending on the process (see below).

## Adding batch process {#batch-process-add}

### Creating the processing file

Batch process are defined on a per schema basis. To check the list of available process for a standard check the `<datadirectory>/config/schemaPlugins/<schemaId>/process` folder.

The `process` folder contains a set of XSLT. The name of the XSLT file without extension is used to trigger the process. For example, if using `md.processing.batch?process=my-custom-process`, the process XSLT MUST be named `my-custom-process.xsl`.

### Processing the XML of the record

The XSLT process will be applied to each metadata record in the selection. Each document will have as root element the metadata XML document with the `geonet:info` element. The `geonet:info` element contains metadata about the metadata. This element MUST be removed by the process to not alter the record when saved in the database.

``` xml
<gmd:MD_Metadata>
    ...
    <geonet:info xmlns:geonet="http://www.fao.org/geonetwork">
        <id>73481</id>
        <uuid>bb151890-2da5-4cfb-8659-7839e7138be7</uuid>
        <schema>iso19139</schema>
        <createDate>2015-12-23T17:05:36</createDate>
        <changeDate>2015-12-23T18:07:40</changeDate>
        <source>2cc603e1-981c-41a2-a183-39429c7dcc49</source>
        <ownerId>1</ownerId>
        <edit>true</edit>
        <owner>true</owner>
        <isPublishedToAll>false</isPublishedToAll>
        <view>true</view>
        <notify>true</notify>
        <download>true</download>
        <dynamic>true</dynamic>
        <featured>true</featured>
        <selected>true</selected>
    </geonet:info>
</gmd:MD_Metadata>
```

### Adding parameters

The XSLT process can retrieve parameters which may be provided in the URL using `xsl:param`. For example, if using `md.processing.batch?process=my-custom-process&myParameter=test`.

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                exclude-result-prefixes="#all">

    <xsl:param name="myParameter" select="''"/>
```

In this example, `xsl:param` will be accessible as a variable using `$myParameter` and it will have an empty value if not set by the URL parameter. To check that a parameter is properly set, use `xsl:message` to output information to the log file.

``` xml
<xsl:param name="myParameter" select="''"/>
<xsl:message>myParameter: <xsl:value-of select="$myParameter"/></xsl:message>
```

The XSLT process also have access to catalog parameters:

-   guiLang: Current UI language
-   baseUrl: The service base URL (eg `http://localhost:8080/geonetwork`)
-   catalogUrl: The catalog URL (eg `http://localhost:8080/geonetwork/srv/eng`)
-   nodeId: The node identifier (default `srv`)

To use one of those parameters in the process, use `xsl:param`:

``` xml
<xsl:param name="guiLang" select="''"/>
```

### Making a copy is a minimum

A process MUST at least do:

-   a copy of everything
-   remove geonet:info metadata

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:geonet="http://www.fao.org/geonetwork" version="2.0"
                exclude-result-prefixes="#all">

    <!-- Do a copy of every nodes and attributes recursively -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Remove geonet:* elements. -->
    <xsl:template match="geonet:*" priority="2"/>
</xsl:stylesheet>
```

Then customize the process to update the metadata record.

### Processing examples

See `schemas/iso19139/src/main/plugin/iso19139/process` for examples.

On top of the minimum that the process MUST do, the process can define additional actions using new templates:

-   Removing an element. Eg. removing all report about DQ_TopologicalConsistency:

``` xml
<xsl:template match="gmd:report[gmd:DQ_TopologicalConsistency]"
              priority="2"/>
```

Set priority to 2 in order for your template to take priority over the main template making the copy of everything.

## Registering a process as a suggestion {#customizing-xslt-suggestion}

See [Suggestion for improving metadata content](suggestion.md).

## Registering a process as an editor action {#xslt-in-editor}

An XSLT process can be used in the editor to trigger specific actions. For example, the INSPIRE view display a button to add a resource identifier if none defined ending by the metadata identifier.

``` xml
<action type="batch"
        process="add-resource-id"
        if="count(gmd:MD_Metadata/gmd:identificationInfo/*/
                      gmd:citation/gmd:CI_Citation/
                          gmd:identifier[
                          ends-with(
                              gmd:MD_Identifier/gmd:code/gco:CharacterString,
                              //gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString
                          )]) = 0"/>
```

See ref:``creating-custom-editor``.

## Adding XSLT conversion for import {#customizing-xslt-conversion}

Add XSL transformations to `web/geonetwork/xsl/conversion/import` folder in order to provide new import options to the user. Files can be added to this folder without restarting the application.
