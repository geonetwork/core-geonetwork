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

When using the GetRecords operation for searching, 2 types of parameter can be use for searching:

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

## Upgrading from GeoNetwork 3.0 Guidance

The configuration of "Virtual CSW" end-points are replaced by [sub-portals](../administrator-guide/configuring-the-catalog/portal-configuration.md).
