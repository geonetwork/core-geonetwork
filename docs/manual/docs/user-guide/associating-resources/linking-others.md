# Other types of resources (eg. sensor, publication, revision) {#linking-others}

In ISO, associated resource allows to link to record defining the type of relation with:

* association type (mandatory)
* initiative type

Codelist values for association types are:

* Cross reference
* Larger work citation
* Part of seamless database
* Stereo mate
* Is composed of
* Collective Title
* Series
* Dependency
* Revision Of


![](img/iso-associated-resources.png)

## Grouping records together

When having a series of records related together, it may make sense to group them under a same parent record.

In ISO19115-3, by default parent relations are defined with association type set to `partOfSeamlessDatabase` to indicate that a set of datasets are stored in same database.
This can be customized with [the panel configuration](linking-panel-configuration.md).

To promote some kind of "data products" or "collections", it may be interesting to create parent records with association type pointing to the child records with association type `isComposedOf`. This is the approach used at EEA (https://sdi.eea.europa.eu/catalogue) where all datasets are grouped in series. The series provide general information and quick access to all child records (eg. temporal series, current and archived versions).



## Linking to previous version

When a record is updated, it may be interesting to link the new version to the previous version.  
This can be done with the association type `revisionOf` and linking to the previous version of the record:

```xml
<mri:associatedResource>
  <mri:MD_AssociatedResource>
     <mri:associationType>
        <mri:DS_AssociationTypeCode codeListValue="revisionOf"/>
     </mri:associationType>
     <mri:metadataReference uuidref="481df889-4f3d-4290-bd89-b7f3ad11a2f1"
                            xlink:href="http://localhost:8080/geonetwork/srv/api/records/481df889-4f3d-4290-bd89-b7f3ad11a2f1"/>
  </mri:MD_AssociatedResource>
</mri:associatedResource>
```

This will allow users to navigate between versions of the record in the record view or using the related record API.

It is recommended to use the same privileges for all versions of the record to be able to navigate between all versions. Usually when a revision is created, the previous version status is updated to "archived" or "superseded".



## Linking to scientific publications

When a dataset is linked to a scientific publication, an option is to use a link with the association type `cross reference` and initiative type set to `study`.
Those type of links usually point to a DOI or a remote URL (See [link to remote URL](linking-remote-records.md) and [using DataCite and Crossref for DOI](linking-panel-configuration.md)).

