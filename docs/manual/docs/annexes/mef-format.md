# Metadata Exchange Format (MEF) {#mef_format}

## Introduction

The metadata exchange format (MEF in short) is a specially designed file format for the purpose of metadata exchange between different platforms. A metadata exported as a MEF can be imported by any platform which is able to understand MEF. This format has been developed with GeoNetwork in mind so the information it contains is mainly related to GeoNetwork. Nevertheless, it can be used as an interoperability format between different platforms.

This format has been designed with the following needs in mind:

1.  Export a metadata record for backup purposes
2.  Import a metadata record from a previous backup
3.  Import a metadata record from a different GeoNetwork version to allow a smooth migration from one version to another.
4.  Capture metadata plus thumbnails and any data uploaded with the metadata record.

In the paragraphs below, some terms should be intended as follows:

1.  the term actor is used to indicate any system (application, service etc.) that operates on metadata.
2.  the term reader will be used to indicate any actor that can import metadata from a MEF file.
3.  the term writer will be used to indicate any actor that can generate a MEF file.

## Requesting a specific version via the API {#requesting-a-version}

The catalog's REST API can export any of the three versions described below from the same two endpoints. Which version is returned is negotiated with the standard HTTP `Accept` header, using one of the following media types:

| `Accept` header value       | Version returned |
|------------------------------|-------------------|
| `application/x-gn-mef-1-zip` | MEF v1            |
| `application/x-gn-mef-2-zip` | MEF v2            |
| `application/x-gn-mef-3-zip` | MEF v3            |

If the `Accept` header is missing, or set to anything else (eg. a generic `application/zip`, or `*/*`), the export defaults to MEF v3 - the current, recommended version. The response's `Content-Type` header always reflects the version actually returned, so a client can confirm what it received without inspecting the file itself.

Two endpoints support this negotiation:

-   `GET /{portal}/api/records/{metadataUuid}/formatters/zip` - export a single record. Supports all three versions.
-   `GET /{portal}/api/records/zip` - export a selection of records (via a `uuids` parameter, or the current search selection) as a single file. Only MEF v2 and v3 are supported here, since a v1 archive can only ever hold one record; requesting v1 on this endpoint returns an error.

For example, to download a single record as a MEF v2 archive (the legacy public/private layout):

``` text
curl -H "Accept: application/x-gn-mef-2-zip" \
     "https://my-catalog.example.org/geonetwork/srv/api/records/0619abc0-708b-eeda-8202-000d98959033/formatters/zip" \
     -o record.zip
```

## MEF v1 file format

A MEF file is simply a ZIP file which contains the following files:

``` text
Root
 |
 +--- metadata.xml
 +--- info.xml
 +--- store
          +---- all documents and thumbnails, public and private alike
```

1.  *metadata.xml*: this file contains the metadata itself, in XML format. The text encoding of the metadata (eg. UTF-8) is specified in the XML declaration.
2.  *info.xml*: this is a special XML file which contains information related to the metadata (metadata about the metadata). Examples of the information in the info.xml file are: creation date, modification date, privileges This information is needed by GeoNetwork.
3.  *store* (version 3.0 and above): this is a single directory used to store the metadata thumbnails and every other file uploaded with the metadata in the GeoNetwork editor (maps, shape files etc.), public and private alike. There are no restrictions on the file format or type. Whether a file is public or private is not determined by its location in this directory: it is recorded per file, via the access attribute on the corresponding file element of info.xml's store element (see [The info.xml file](#the-info-xml-file) below), and it is the reader's responsibility to enforce any authorisation this implies.

    Prior to version 3.0, public and private files were kept in two separate top-level directories instead of this single store directory:

    ``` text
    Root
     |
     +--- metadata.xml
     +--- info.xml
     +--- public
     |        +---- all public documents and thumbnails
     +--- private
               +---- all private documents and thumbnails
    ```

    -   *public* (pre-3.0): a directory used to store the metadata thumbnails and other public files. There are no restrictions on the image format but it is strongly recommended to use the portable network graphics (PNG), JPEG or GIF format.
    -   *private* (pre-3.0): a directory used to store all data (maps, shape files etc.) uploaded with the metadata in the GeoNetwork editor. Files in this directory are *private* in the sense that authorisation is required to access them. There are no restrictions on the file types that can be stored into this directory.

    A reader capable of reading version 3.0 archives must continue to support archives written in this pre-3.0 layout, since the version policy in the introduction to this section only guarantees forward compatibility for readers, not that every writer has moved to the newer minor version. Writers should always produce the store-based layout.

Any other file or directory present in the MEF file should be ignored by readers that don't recognise them. This allows actors to add custom extensions to the MEF file.

A MEF file can have an empty store folder (or, for pre-3.0 archives, empty public and private folders) depending upon the export format, which can be:

-   *simple*: no files are provided.
-   *partial*: only public files are provided.
-   *full*: both public and private files are provided.

It is recommended to use the .mef extension when naming MEF files.

## MEF v2 file format

MEF version 2 supports the following:

-   multi-metadata support: more than one metadata record and data can be stored in a single MEF file.
-   multi-schema support: be able to store in a single MEF n formats (eg. for an ISO profile, also store a version of that record in the base ISO19115/ISO19139 schema).

Current export services that create MEF files from a metadata record with related records (e.g. parent, feature catalog, etc.) can include these related metadata records in the MEF.

MEF v2 format structure is the following:

``` text
Root
  |
 + 0..n metadata
     |
     +--- metadata
     |     +--- metadata.xml
     |     +--- (optional) metadata.iso19139.xml
     +--- info.xml
     +--- applschema
     |     +--- (optional) metadata.xml (ISO19110 Feature Catalog)
     +--- store
           +---- all documents and thumbnails, public and private alike
```

!!! note

    metadata.iso19139.xml is generated by GeoNetwork actors on export if the metadata record in metadata.xml is an ISO19115/19139 profile. On import, this record may be selected for loading if the ISO19115/19139 profile is not present.

Prior to version 3.0, the store directory shown above was instead two separate public and private directories, exactly as described for the [MEF v1 file format](#mef-v1-file-format) above; the same pre-3.0/3.0 compatibility rules apply here.

### Nested folders {#nested-folders}

Files in the store directory (or, for pre-3.0 archives, the public and private directories) may be organised into subfolders (eg. `store/images/thumbnail.png`). A reader must recursively walk the directory rather than assume a flat file listing. The file's name, as registered in info.xml (see below), is its path relative to the store directory (or, for pre-3.0 archives, relative to the public or private directory), with `/` as the separator regardless of the writer's operating system.

This applies to both the MEF v1 and MEF v2 container layouts described above; it is independent of the info.xml version.

## The info.xml file

This file contains general information about a metadata. It must have an info root element with a mandatory version attribute. This attribute must be in the X.Y form, where X represents the major version and Y the minor one. The purpose of this attribute is to allow future changes of this format maintaining compatibility with older readers. The policy behind the version is this:

1.  A change to Y means a minor change. All existing elements in the previous version must be left unchanged: only new elements or attributes may be added. A reader capable of reading version X.Y is also capable of reading version X.Y' with Y'>Y.
2.  A change to X means a major change. Usually, a reader of version X.Y is not able to read version X'.Y with X'>X.

The root element must have the following children:

1.  *general*: a container for general information. It must have the following children:
    -   *uuid*: this is the universally unique identifier assigned to the metadata and must be a valid UUID. This element is optional and, when omitted, the reader should generate one. A metadata without a UUID can be imported several times into the same system without breaking uniqueness constraints. When missing, the reader should also generate the siteId value.
    -   *createDate*: This date indicates when the metadata was created.
    -   *changeDate*: This date keeps track of the most recent change to the metadata.
    -   *siteId*: This is an UUID that identifies the actor that created the metadata and must be a valid UUID. When the UUID element is missing, this element should be missing too. If present, it will be ignored.
    -   *siteName*: This is a human readable name for the actor that created the metadata. It must be present only if the siteId is present.
    -   *schema*: The name of the schema for the metadata record in metadata.xml. When the MEF is imported by a GeoNetwork actor, this name should be the name of a metadata schema handled by the actor (eg. iso19139). If the GeoNetwork actor does not have such a schema, it may try and select another metadata with a schema that is present (eg. the metadata in metadata-iso19139.xml could be loaded because the iso19139 schema is present).
    -   *format*: Indicates the MEF export format. The element's value must belong to the following set: { *simple*, *partial*, *full* }.
    -   *localId*: This is an optional element. If present, indicates the id used locally by the sourceId actor to store the metadata. Its purpose is just to allow the reuse of the same local id when reimporting a metadata.
    -   *isTemplate*: A boolean field that indicates if this metadata is a template used to create new ones. There is no real distinction between a real metadata and a template but some actors use it to allow fast metadata creation. The value must be: { *true*, *false* }.
    -   *rating*: This is an optional element. If present, indicates the users' rating of the metadata ranging from 1 (a bad rating) to 5 (an excellent rating). The special value 0 means that the metadata has not been rated yet. Can be used to sort search results.
    -   *popularity*: Another optional value. If present, indicates the popularity of the metadata. The value must be positive and high values mean high popularity. The criteria used to set the popularity is left to the writer. Its main purpose is to provide a metadata ordering during a search.
2.  *categories*: a container for categories associated to this metadata. A category is just a name, like 'audio-video' that classifies the metadata to allow an easy search. Each category is specified by a category element which must have a name attribute. This attribute is used to store the category's name. If there are no categories, the categories element will be empty.
3.  *privileges*: a container for privileges associated to this metadata. Privileges are operations that a group (which represents a set of users) can do on a metadata and are specified by a set of group elements. Each one of these, has a mandatory name attribute to store the group's name and a set of operation elements used to store the operations allowed on the metadata. Each operation element must have a name attribute which value must belong to the following set: { *view*, *download*, *notify*, *dynamic*, *featured* }. If there are no groups or the actor does not have the concept of group, the privileges element will be empty. A group element without any operation element must be ignored by readers.
4.  *store* (version 3.0 and above): A single container listing every file in the store directory (thumbnails, maps, and any other uploaded document), public and private alike. This container contains a file element for each file. Mandatory attributes of this element are name, which represents the file's name (its path relative to the store directory, see [Nested folders](#nested-folders)), changeDate, which contains the date of the latest change to the file, and access, whose value must belong to the following set: { *public*, *private* } and indicates whether the file is public or private - a matter of this attribute alone, since (from version 3.0 on) the store directory itself has no public/private split. The mimetype attribute is optional; when present, it records the file's content type. The store element is optional but, if present, must list all the files present in the metadata's store directory, and any reader that imports these files must set the latest change date and, if provided, mimetype on these using the provided values. The purpose of this element is to provide more information in the case the MEF format is used for metadata harvesting.

    Prior to version 3.0, this information was split across two separate elements, public and private, described below, matching the pre-3.0 physical layout's own public/private split (see [MEF v1 file format](#mef-v1-file-format)). A reader capable of reading version 3.0 must continue to support archives that use the pre-3.0 form, since the version policy in the introduction to this section only guarantees forward compatibility for readers, not that every writer has moved to the newer minor version.

    -   *public* (pre-3.0): All metadata thumbnails (and any other public file) must be listed here, using the same file element and name/changeDate attributes described above (no access or mimetype attribute). The public element is optional but, if present, must contain all the files present in the metadata's public directory.
    -   *private* (pre-3.0): This element has the same purpose and structure as the pre-3.0 public element but is related to maps and all other private files.

Any other element or attribute should be ignored by readers that don't understand them. This allows actors to add custom attributes or subtrees to the XML.

### Date format {#info_xml}

Unless otherwise specified, all dates in this file must be in the ISO 8601 format. The pattern must be ``YYYY-MM-DDTHH:mm:SS`` and the timezone should be the local one.

Example of an info file (version 3.0, unified store element):

``` xml
<info version="3.0">
    <general>
        <uuid>0619abc0-708b-eeda-8202-000d98959033</uuid>
        <createDate>2006-12-11T10:33:21</createDate>
        <changeDate>2006-12-14T08:44:43</changeDate>
        <siteId>0619cc50-708b-11da-8202-000d9335906e</siteId>
        <siteName>FAO main site</siteName>
        <schema>iso19139</schema>
        <format>full</format>
        <localId>204</localId>
        <isTemplate>false</isTemplate>
    </general>
    <categories>
        <category name="maps"/>
        <category name="datasets"/>
    </categories>
    <privileges>
        <group name="editors">
            <operation name="view"/>
            <operation name="download"/>
        </group>
    </privileges>
    <store>
        <file name="small.png" changeDate="2006-10-07T13:44:32" access="public" mimetype="image/png"/>
        <file name="large.png" changeDate="2006-11-11T09:33:21" access="public" mimetype="image/png"/>
        <file name="map.zip" changeDate="2006-11-12T13:23:01" access="private" mimetype="application/zip"/>
    </store>
</info>
```

Example of a pre-3.0 info file (separate public/private elements):

``` xml
<info version="1.0">
    <general>
        <uuid>0619abc0-708b-eeda-8202-000d98959033</uuid>
        <createDate>2006-12-11T10:33:21</createDate>
        <changeDate>2006-12-14T08:44:43</changeDate>
        <siteId>0619cc50-708b-11da-8202-000d9335906e</siteId>
        <siteName>FAO main site</siteName>
        <schema>iso19139</schema>
        <format>full</format>
        <localId>204</localId>
        <isTemplate>false</isTemplate>
    </general>
    <categories>
        <category name="maps"/>
        <category name="datasets"/>
    </categories>
    <privileges>
        <group name="editors">
            <operation name="view"/>
            <operation name="download"/>
        </group>
    </privileges>
    <public>
        <file name="small.png" changeDate="2006-10-07T13:44:32"/>
        <file name="large.png" changeDate="2006-11-11T09:33:21"/>
    </public>
    <private>
        <file name="map.zip" changeDate="2006-11-12T13:23:01"/>
    </private>
</info>
```
