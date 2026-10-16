# OGC Catalog Service (CSW) {#csw-api}

The CSW end point exposes the metadata records in your catalog in XML format using the OGC CSW protocol (version 2.0.2).

Two Catalogue Service profiles are available:

-   Catalogue Services for the Web (CSW): Provides the ability to search and publish metadata for data, services and related information.
-   Catalogue Services for the Web Transaction (CSW-T): Provides additional operations for creating, modifying and deleting catalog records via the CSW protocol.

Reference:

* [Catalogue Service](https://www.ogc.org/standard/cat/) (OGC)

## Configuration

See [Configuring CSW](../administrator-guide/configuring-the-catalog/csw-configuration.md) for details of how to configure the CSW end point.

## URL

The following URL is the standard end point for the catalog (substitute your GeoNetwork URL):

-   <http://localhost:8080/geonetwork/srv/eng/csw>?

Generally, the `VERSION` and `SERVICE` parameter are also added, along with the `REQUEST` parameter as detailed below:

-   <http://localhost:8080/geonetwork/srv/eng/csw?SERVICE=CSW&VERSION=2.0.2&REQUEST=GetCapabilities>

## Requests

The full set of requests supported by GeoNetwork can be found in `CSW test`, in the `Settings` section of the Admin Dashboard.

See [Configuring CSW](../administrator-guide/configuring-the-catalog/csw-configuration.md) for more details of this function.

When using the GetRecords operation for searching, two types of parameter can be used:

-   The list of queryable listed in the GetCapabilities document
-   The fields in the index

Example of a request using a standard queryable:

``` xml
<csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                service="CSW" version="2.0.2">
  <csw:Query typeNames="csw:Record">
    <csw:Constraint version="1.1.0">
      <Filter xmlns="http://www.opengis.net/ogc">
        <PropertyIsEqualTo>
          <PropertyName>OnlineResourceType</PropertyName>
          <Literal>OGC:WFS-1.1.0-http-get-feature</Literal>
        </PropertyIsEqualTo>
      </Filter>
    </csw:Constraint>
  </csw:Query>
</csw:GetRecords>
```

Example of a request using an index field name:
{: #csw-index-field-example }

``` xml
<csw:GetRecords xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
                service="CSW" version="2.0.2">
  <csw:Query typeNames="csw:Record">
    <csw:Constraint version="1.1.0">
      <Filter xmlns="http://www.opengis.net/ogc">
        <PropertyIsEqualTo>
          <PropertyName>linkProtocol</PropertyName>
          <Literal>OGC:WMS</Literal>
        </PropertyIsEqualTo>
      </Filter>
    </csw:Constraint>
  </csw:Query>
</csw:GetRecords>
```

The mapping between CSW standard queryable and the index fields are defined in **`web/src/main/webapp/WEB-INF/config-csw.xml`**.

## Queryables mapping

The following tables list the CSW queryables, the ISO field they refer to and the index field used for searching.

!!! note

    In addition to these queryables, any Elasticsearch index field can be used as a property name in a `GetRecords` query (see the [example of a request using an index field name](#csw-index-field-example)). These fields are not reported in the `GetCapabilities` document.

### Supported ISO queryables

| CSW queryable | ISO field | Elasticsearch index field |
|---|---|----------------------------------------------------------|
| `Subject` | Keyword | `tag.*`                                                  |
| `Title` | Resource title | `resourceTitleObject.*`                                  |
| `Abstract` | Abstract | `resourceAbstractObject.*`                               |
| `AnyText` | Full text (any field) | `any.*`                                                  |
| `Format` | Distribution format name | `format`                                                 |
| `Identifier` | Metadata identifier (UUID) | `uuid`                                                   |
| `Modified` | Metadata date stamp | `changeDate`                                             |
| `Type` | Hierarchy level (resource type) | `resourceType`                                           |
| `RevisionDate` | Resource revision date | `revisionDateForResource`                                |
| `AlternateTitle` | Alternate title | `resourceAltTitleObject.*`                               |
| `CreationDate` | Resource creation date | `creationDateForResource`                                |
| `PublicationDate` | Resource publication date | `publicationDateForResource`                             |
| `OrganisationName` | Resource point of contact organisation | `OrgForResourceObject.*`                                 |
| `HasSecurityConstraints` | Has security constraints | `secConstr`                                              |
| `Language` | Metadata language | `mainLanguage`                                           |
| `ResourceIdentifier` | Resource identifier | `resourceIdentifier.code`                                |
| `ParentIdentifier` | Parent identifier | `parentUuid`                                             |
| `KeywordType` | Keyword type | `cl_type.key`                                            |
| `GeographicDescriptionCode` | Geographic description code | `keywordType-place.*`                                    |
| `TopicCategory` | Topic category | `cl_topic.key`                                           |
| `ResourceLanguage` | Resource language | `resourceLanguage`                                       |
| `Denominator` | Spatial resolution (scale denominator) | `resolutionScaleDenominator`                             |
| `DistanceValue` | Spatial resolution (distance) | `resolutionDistance`                                     |
| `DistanceUOM` | Spatial resolution distance unit | `distanceUom`                                            |
| `TempExtent_begin` | Temporal extent begin | `resourceTemporalDateRange.gte`                          |
| `TempExtent_end` | Temporal extent end | `resourceTemporalDateRange.lte`                          |
| `ServiceType` | Service type | `serviceType`                                            |
| `ServiceTypeVersion` | Service type version | `serviceTypeVersion`                                     |
| `Operation` | Service operation name | `operation`                                              |
| `OperatesOn` | Operates on (dataset UUID) | `recordOperateOn`                                        |
| `OperatesOnIdentifier` | Coupled resource identifier | `operatesOnIdentifier`                                   |
| `OperatesOnName` | Coupled resource operation name | `operatesOnName`                                         |
| `CouplingType` | Coupling type | `couplingType`                                           |

### Additional queryables (INSPIRE and others)

| CSW queryable | ISO field | Elasticsearch index field |
|---|---|----------------------------------------------------------|
| `Degree` | Conformity degree (pass) | `inspireConformResource`                                 |
| `SpecificationTitle` | Conformity specification title | `specificationConformance.title`                         |
| `SpecificationDate` | Conformity specification date | `specificationConformance.date`                          |
| `SpecificationDateType` | Conformity specification date type | `specificationDateType`                                  |
| `AccessConstraints` | Access constraints | `cl_accessConstraints.key`                               |
| `OtherConstraints` | Other constraints | `MD_LegalConstraintsOtherConstraintsObject.*`            |
| `Classification` | Security classification | `cl_classification.key`                                  |
| `ConditionApplyingToAccessAndUse` | Use limitation | `MD_ConstraintsUseLimitationObject.*`                    |
| `MetadataPointOfContact` | Metadata point of contact organisation | `OrgObject.*`                                            |
| `Lineage` | Lineage statement | `lineageObject.*`                                        |
| `ResponsiblePartyRole` | Responsible party role | `contactForResource.role`                                |
| `OnlineResourceType` | Online resource protocol | `linkProtocol`                                           |
| `OnlineResourceMimeType` | Online resource MIME type | `mimetype`                                               |
| `Date` | Metadata creation date | `createDate`                                             |
| `Relation` | Related resources | `relation`                                               |

## Upgrading from GeoNetwork 3.0 Guidance

The configuration of "Virtual CSW" end-points are replaced by [sub-portals](../administrator-guide/configuring-the-catalog/portal-configuration.md).
