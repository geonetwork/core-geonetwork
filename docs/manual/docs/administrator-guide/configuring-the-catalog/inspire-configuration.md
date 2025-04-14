# Configuring for the INSPIRE Directive {#inspire-configuration}

## Enabling INSPIRE

From the **Admin console --> Settings** user can configure INSPIRE directive support.

When enabled, the INSPIRE support activate the following:

-   Enable indexing of INSPIRE themes and annexes (INSPIRE themes thesaurus MUST be added to the list of thesaurus from the INSPIRE Registry - see [Managing thesaurus](../managing-classification-systems/managing-thesaurus.md)).

    ![image](img/inspire-configuration.png)

To configure the discovery service, a dedicated service metadata record MUST be created in order to provide a complete GetCapabilities document ([CSW Configuration for INSPIRE](csw-configuration.md#csw-configuration_inspire)).

## Loading INSPIRE codelists

To describe INSPIRE datasets and series, it is recommended to load relevant codelists from the [INSPIRE Registry](https://inspire.ec.europa.eu/registry/), the following codelists are relevant in the scope of metadata guidelines v2.0:

-   [INSPIRE Theme](https://inspire.ec.europa.eu/theme)
-   [Application schema](https://inspire.ec.europa.eu/applicationschema)
-   [Media types](https://inspire.ec.europa.eu/media-types)
-   Metadata codelist register --> [Protocols](https://inspire.ec.europa.eu/metadata-codelist/ProtocolValue)
-   Metadata codelist register --> [Spatial scope](https://inspire.ec.europa.eu/metadata-codelist/SpatialScope)
-   Metadata codelist register --> [INSPIRE priority data set](https://inspire.ec.europa.eu/metadata-codelist/PriorityDataset)
-   Metadata codelist register --> [Spatial Data Service Category](https://inspire.ec.europa.eu/metadata-codelist/SpatialDataServiceCategory)
-   Metadata codelist register --> [Conditions applying to Access and Use](https://inspire.ec.europa.eu/metadata-codelist/ConditionsApplyingToAccessAndUse)
-   Metadata codelist register --> [Limitations on Public Access](https://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess)
-   Metadata codelist register --> [OnLine Description Code](https://inspire.ec.europa.eu/metadata-codelist/OnLineDescriptionCode)
-   Metadata codelist register --> [Quality of Service Criteria](https://inspire.ec.europa.eu/metadata-codelist/QualityOfServiceCriteria)

From `Admin console` --> `Classification systems` --> `Thesaurus`, administrators can manage thesauri. One of the options is to load a thesaurus straight from the registry.

![image](img/inspire-from-registry.png)

Click `Use INSPIRE registry` to use the default INSPIRE registry but any instance of the [Registry software](https://joinup.ec.europa.eu/solution/re3gistry) can be used.

![image](img/inspire-from-registry-config.png)

Select one or more languages depending on your needs. Choose a category or directly a thesaurus, depending on the thesaurus. By default the type of thesaurus will be `Theme` but you can adapt it if needed.

By clicking the `Upload` button the catalogue will contact the registry, download the files for each languages and combined them in a thesaurus in SKOS format supported by the catalogue.

User can also use the well known [GEMET thesaurus](https://www.eionet.europa.eu/gemet/en/themes/). Some SKOS format version of the thesaurus are available [here](https://github.com/geonetwork/util-gemet/tree/master/thesauri).

Once loaded, a thesaurus can be used in metadata records to select keywords from:

![image](img/inspire-keyword-editing.png)

The type of encoding of keywords can be defined using the gear icon (See validation section for more information):

![image](img/inspire-keyword-encoding-type.png)

Via the schema plugin form configuration it is an option to configure a thesaurus to be used for a specific `Anchor` element. The thesaures concepts are used to populate an auto complete text field for that element.

## INSPIRE validation

INSPIRE validation of metadata records is available at [the INSPIRE Validator](https://inspire.ec.europa.eu/validator/about/). It is using [ETF which is an open source testing framework for spatial data and services](https://github.com/etf-validator/etf-webapp). GeoNetwork is able to `remote validate` any record using a service provided by an instance of ETF. To configure remote validation, go to `Admin console` --> `Settings` and set the URL of the validator. The url of the main INSPIRE validator is `https://inspire.ec.europa.eu/validator/`.

![image](img/inspire-configuration.png)

Once enabled, the editor will show the remote validation option in the menu:

![image](img/inspire-validation-menu.png)

The standard validate option will use the internal validation system (ie. XSD, Schematron rules for ISO, INSPIRE, \... depending on the configuration). In the internal system the INSPIRE validation is based on INSPIRE Technical guidance version 1.3 and results will be different from ETF reports.

The remote INSPIRE validation will open the validator in a popup. Choose one of the options depending on the level of validation and type of resource to validate. The list of options can be customized in [this configuration file](https://github.com/geonetwork/core-geonetwork/blob/master/services/src/main/resources/config-spring-geonetwork.xml#L61-L94). The configuration is made by selecting one or more test suite from the ETF options:

![image](img/inspire-etf-test-configuration.png)

During the validation, the record is sent to the ETF service and processed. Once ETF completes the validation, the catalogue will display a link to the validation report.

![image](img/inspire-validation-report.png)

Note, that if you are validating a private record, that record will be pushed to the validator. To secure this process we recommend to set up a local (private) installation of the validator.

### Configure validation test suites

The set of test that runs for each schema can be configured using the file [WEB-INF/config-etf-validator.xml](https://github.com/geonetwork/core-geonetwork/blob/5156bae32d549e6d09cd6a86065791265eb09027/web/src/main/webapp/WEB-INF/config-etf-validator.xml).

The list of available test suites are defined in the `inspireEtfValidatorTestsuites` bean. It is a map with an entry for each test suite. The `key` attribute is the name of the test suite. Each map entry is an `array` with the tests to execute in the test suite. The value of each array item (`<value>`)is the test's title written exactly as defined in the remote INSPIRE validator service. For example:

``` xml
<util:map id="inspireEtfValidatorTestsuites" key-type="java.lang.String" value-type="java.lang.String[]">
 <entry key="TG version 1.3">
   <array value-type="java.lang.String">
     <value>Conformance class: INSPIRE Profile based on EN ISO 19115 and EN ISO 19119</value>
     <value>Conformance class: XML encoding of ISO 19115/19119 metadata</value>
     <value>Conformance class: Conformance class: Metadata for interoperability</value>
   </array>
 </entry>
 <entry key="TG version 2.0 - Data sets and series">
   <array value-type="java.lang.String">
     <value>Common Requirements for ISO/TC 19139:2007 based INSPIRE metadata records.</value>
     <value>Conformance Class 1: INSPIRE data sets and data set series baseline metadata.</value>
     <value>Conformance Class 2: INSPIRE data sets and data set series interoperability metadata.</value>
   </array>
 </entry>
 <entry key="TG version 2.0 - Network services">
   <array value-type="java.lang.String">
     <value>Common Requirements for ISO/TC 19139:2007 based INSPIRE metadata records.</value>
     <!--<value>Conformance Class 1: INSPIRE data sets and data set series baseline metadata.</value>
     <value>Conformance Class 2: INSPIRE data sets and data set series interoperability metadata.</value>-->
     <value>Conformance Class 3: INSPIRE Spatial Data Service baseline metadata.</value>
     <value>Conformance Class 4: INSPIRE Network Services metadata.</value>
     <!--<value>Conformance Class 5: INSPIRE Invocable Spatial Data Services metadata.</value>
     <value>Conformance Class 6: INSPIRE Interoperable Spatial Data Services metadata.</value>
     <value>Conformance Class 7: INSPIRE Harmonised Spatial Data Services metadata.</value>-->
   </array>
 </entry>
</util:map>
```

Array's `value-type` attribute must be defined as Java strings: `<array value-type="java.lang.String">`.

To define which test suites will be executed when using the editor dashboard's INSPIRE validation option you can modify the `inspireEtfValidatorTestsuitesConditions` bean. It's a map with an entry for each schema and test suite to execute. The map entry key attribute must be in the format `SCHEMA_ID::TEST_SUITE_NAME`, where `TEST_SUITE_NAME` is one of the `inspireEtfValidatorTestsuites` map entry key. For each entry you can define a XPath condition that the metadata must pass to be sent to the validator.

!!! note

    If a metadata schema doesn't match, the schema dependency hierarchy is checked to verify if any parent schema matches any rules.


!!! warning

    The Xpath must return a node-set or a node to work. XPaths returning a boolean `true` or `false` value will be interpreted as always matching by GeoNetwork.


``` xml
<util:map id="inspireEtfValidatorTestsuitesConditions">
  <!--
     key format:
     SCHEMAID::TG_RULE_NAME
     If a metadata schema doesn't match, the schema dependency hierarchy
     is checked to verify if any parent schema matches any rules.
    -->
  <entry key="iso19139::TG version 2.0 - Data sets and series"
         value="gmd:hierarchyLevel[*/@codeListValue = 'dataset' or */@codeListValue = 'series']"/>
  <entry key="iso19139::TG version 2.0 - Network services" value=".//srv:SV_ServiceIdentification"/>
  <entry key="iso19115-3.2018::TG version 2.0 - Data sets and series"
         value="mdb:metadataScope[*/mdb:resourceScope/*/@codeListValue = 'dataset' or */mdb:resourceScope/*/@codeListValue = 'series']"/>
  <entry key="iso19115-3.2018::TG version 2.0 - Network services" value=".//srv:SV_ServiceIdentification"/>
</util:map>
```

## INSPIRE access point

In many cases only a part of the metadata records in a catalog are related to the INSPIRE Directive. In that case, it may be relevant to filter the set of records falling in the scope of the Directive and promote them through a sub portal. In this way a European portal can easily harvest the records related to INSPIRE.

First define a filtering mechanism to identify the records in the scope of the directive. Frequently used method are:

-   Create a group `INSPIRE` and publish those record in that group (or a category).
-   Add a specific keyword in the metadata record.
-   Filter based on the conformance quality report having a reference to the EU directive.

From the `Admin console` --> `Settings` --> `Sources`, an administrator can create a sub portal. Create a portal `inspire` and set the filter to select only records related to INSPIRE (eg. `+_groupPublished:INSPIRE` to select all records published in group INSPIRE).

![image](img/inspire-portal.png)

Once saved, the portal is accessible at <http://localhost:8080/geonetwork/inspire> and the CSW service at <http://localhost:8080/geonetwork/inspire/eng/csw>.

## INSPIRE reference documents

-   [INSPIRE IR](https://inspire.ec.europa.eu/)
-   [INSPIRE Technical Guidelines Metadata v2.0.1](https://inspire.ec.europa.eu/sites/default/files/documents/metadata/inspire-tg-metadata-iso19139-2.0.1.pdf)
-   [INSPIRE validator](https://inspire.ec.europa.eu/validator/)
-   [GeoNetwork at the INSPIRE forum](https://inspire.ec.europa.eu/forum/search?q=geonetwork)
