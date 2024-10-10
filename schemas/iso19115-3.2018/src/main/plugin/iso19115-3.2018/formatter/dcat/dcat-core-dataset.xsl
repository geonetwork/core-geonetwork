<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

  <xsl:import href="dcat-variables.xsl"/>

  <xsl:variable name="unitConversionFactorForMeter"
                as="node()*">
    <unit id="m">1</unit>
    <unit id="km">1000</unit>
    <unit id="cm">.01</unit>
    <unit id="ft">0.3048</unit>
  </xsl:variable>

  <xsl:variable name="unitCodes"
                as="node()*">
    <unit id="m">meter</unit>
    <unit id="m">EPSG::9001</unit>
    <unit id="m">urn:ogc:def:uom:EPSG::9001</unit>
    <unit id="m">urn:ogc:def:uom:UCUM::m</unit>
    <unit id="m">urn:ogc:def:uom:OGC::m</unit>
    <unit id="ft">feet</unit>
    <unit id="ft">EPSG::9002</unit>
    <unit id="ft">urn:ogc:def:uom:EPSG::9002</unit>
    <unit id="ft">urn:ogc:def:uom:UCUM::[ft_i]</unit>
    <unit id="ft">urn:ogc:def:uom:OGC::[ft_i]</unit>
    <unit id="km">kilometer</unit>
    <unit id="cm">centimeter</unit>
  </xsl:variable>

  <xsl:variable name="isoUnitCodelistBaseUri"
                as="xs:string"
                select="'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/uom/ML_gmxUom.xml#'"/>
  <!--
  RDF Property:	dcat:spatialResolutionInMeters
  Definition:	Minimum spatial separation resolvable in a dataset, measured in meters.
  Range:	rdfs:Literal typed as xsd:decimal
  Usage note:	If the dataset is an image or grid this should correspond to the spacing of items.
  For other kinds of spatial datasets, this property will usually indicate the smallest distance between items in the dataset.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:spatialResolution/*/mri:distance">
    <xsl:variable name="unitId"
                  as="xs:string?"
                  select="if (starts-with(*/@uom, $isoUnitCodelistBaseUri))
                            then substring-after(*/@uom, $isoUnitCodelistBaseUri)
                            else */@uom"/>

    <xsl:variable name="knownCode"
                  as="xs:string?"
                  select="$unitCodes[text() = $unitId]/@id"/>

    <xsl:variable name="unit" as="xs:string?"
                  select="if ($knownCode) then $knownCode else */@uom"/>

    <xsl:variable name="conversionFactor"
                  as="xs:decimal?"
                  select="$unitConversionFactorForMeter[@id = $unit]/text()"/>

    <xsl:choose>
      <xsl:when test="($conversionFactor)
                      and */text() castable as xs:decimal">
        <dcat:spatialResolutionInMeters rdf:datatype="http://www.w3.org/2001/XMLSchema#decimal">
          <xsl:value-of select="xs:decimal(.) * $conversionFactor"/>
        </dcat:spatialResolutionInMeters>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment>WARNING: Spatial resolution only supported in meters.
          <xsl:value-of select="concat(*/text(), ' ', */@uom)"/> is ignored (can be related to unknown unit or no
          conversion factor or not a decimal value).
        </xsl:comment>
      </xsl:otherwise>
    </xsl:choose>

    <!-- TODO: GeoDCAT-AP
    <dqv:hasQualityMeasurement>
      <dqv:QualityMeasurement>
        <dqv:isMeasurementOf>
          <dqv:Metric rdf:about="{$geodcatap}spatialResolutionAsDistance"/>
        </dqv:isMeasurementOf>
        <dqv:value rdf:datatype="{$xsd}decimal"><xsl:value-of select="."/></dqv:value>
        <sdmx-attribute:unitMeasure rdf:resource="{$uom-km}"/>
      </dqv:QualityMeasurement>
    </dqv:hasQualityMeasurement>
    -->

  </xsl:template>


  <!--
  RDF Property:	dcterms:accrualPeriodicity
  Definition:	The frequency at which a dataset is published.
  Range:	dcterms:Frequency (A rate at which something recurs)
  Usage note:	The value of dcterms:accrualPeriodicity gives the rate at which the dataset-as-a-whole is updated. This may be complemented by dcat:temporalResolution to give the time between collected data points in a time series.

  Cardinality:
  * ISO 0..n
  * DCAT 0..n
  * DCAT-AP 0..1
  * Mobility DCAT 1..1 (in ISO either use corresponding period eg. P0Y0M0DT1H0M0S or extend the codelist with the proper vocabulary)

  accrualPeriodicity mapping done using the ISO to Dublin core value mapping
  but additional checks are done when ISO records extended the codelist and
  may used the EU Publication Office frequency codes
  or the Mobility DCAT-AP update frequency codes.
  Domain specific codelists take priority over the DC or ISO codelists.

  eg.
  <mmi:MD_MaintenanceFrequencyCode codeListValue="15min"/>

  multipleAccrualPeriodicityAllowed is a parameter that can be set to true to allow multiple accrualPeriodicity values.
  Default to false for EU formatters. true for DCAT.
  -->
  <xsl:variable name="isoFrequencyToDublinCore"
                as="node()*">
    <entry key="continual">CONT</entry>
    <entry key="daily">DAILY</entry>
    <entry key="weekly">WEEKLY</entry>
    <entry key="fortnightly">BIWEEKLY</entry>
    <entry key="monthly">MONTHLY</entry>
    <entry key="quarterly">QUARTERLY</entry>
    <entry key="biannually">ANNUAL_2</entry>
    <entry key="annually">ANNUAL</entry>
    <entry key="irregular">IRREG</entry>
    <entry key="unknown">UNKNOWN</entry>
    <!--
    <entry key="asNeeded"></entry>
    <entry key="notPlanned"></entry>
    -->
  </xsl:variable>


  <!--
  http://publications.europa.eu/resource/authority/frequency
  -->
  <xsl:variable name="euUpdateFrquencyToUri" as="node()*">
    <entry key="BIDECENNIAL">http://publications.europa.eu/resource/authority/frequency/BIDECENNIAL</entry>
    <entry key="TRIDECENNIAL">http://publications.europa.eu/resource/authority/frequency/TRIDECENNIAL</entry>
    <entry key="BIHOURLY">http://publications.europa.eu/resource/authority/frequency/BIHOURLY</entry>
    <entry key="TRIHOURLY">http://publications.europa.eu/resource/authority/frequency/TRIHOURLY</entry>
    <entry key="OTHER">http://publications.europa.eu/resource/authority/frequency/OTHER</entry>
    <entry key="WEEKLY">http://publications.europa.eu/resource/authority/frequency/WEEKLY</entry>
    <entry key="NOT_PLANNED">http://publications.europa.eu/resource/authority/frequency/NOT_PLANNED</entry>
    <entry key="AS_NEEDED">http://publications.europa.eu/resource/authority/frequency/AS_NEEDED</entry>
    <entry key="HOURLY">http://publications.europa.eu/resource/authority/frequency/HOURLY</entry>
    <entry key="QUADRENNIAL">http://publications.europa.eu/resource/authority/frequency/QUADRENNIAL</entry>
    <entry key="QUINQUENNIAL">http://publications.europa.eu/resource/authority/frequency/QUINQUENNIAL</entry>
    <entry key="DECENNIAL">http://publications.europa.eu/resource/authority/frequency/DECENNIAL</entry>
    <entry key="WEEKLY_2">http://publications.europa.eu/resource/authority/frequency/WEEKLY_2</entry>
    <entry key="WEEKLY_3">http://publications.europa.eu/resource/authority/frequency/WEEKLY_3</entry>
    <entry key="UNKNOWN">http://publications.europa.eu/resource/authority/frequency/UNKNOWN</entry>
    <entry key="UPDATE_CONT">http://publications.europa.eu/resource/authority/frequency/UPDATE_CONT</entry>
    <entry key="QUARTERLY">http://publications.europa.eu/resource/authority/frequency/QUARTERLY</entry>
    <entry key="TRIENNIAL">http://publications.europa.eu/resource/authority/frequency/TRIENNIAL</entry>
    <entry key="NEVER">http://publications.europa.eu/resource/authority/frequency/NEVER</entry>
    <entry key="OP_DATPRO">http://publications.europa.eu/resource/authority/frequency/OP_DATPRO</entry>
    <entry key="MONTHLY_2">http://publications.europa.eu/resource/authority/frequency/MONTHLY_2</entry>
    <entry key="MONTHLY_3">http://publications.europa.eu/resource/authority/frequency/MONTHLY_3</entry>
    <entry key="IRREG">http://publications.europa.eu/resource/authority/frequency/IRREG</entry>
    <entry key="MONTHLY">http://publications.europa.eu/resource/authority/frequency/MONTHLY</entry>
    <entry key="DAILY">http://publications.europa.eu/resource/authority/frequency/DAILY</entry>
    <entry key="DAILY_2">http://publications.europa.eu/resource/authority/frequency/DAILY_2</entry>
    <entry key="BIWEEKLY">http://publications.europa.eu/resource/authority/frequency/BIWEEKLY</entry>
    <entry key="CONT">http://publications.europa.eu/resource/authority/frequency/CONT</entry>
    <entry key="BIENNIAL">http://publications.europa.eu/resource/authority/frequency/BIENNIAL</entry>
    <entry key="BIMONTHLY">http://publications.europa.eu/resource/authority/frequency/BIMONTHLY</entry>
    <entry key="ANNUAL_2">http://publications.europa.eu/resource/authority/frequency/ANNUAL_2</entry>
    <entry key="ANNUAL_3">http://publications.europa.eu/resource/authority/frequency/ANNUAL_3</entry>
    <entry key="ANNUAL">http://publications.europa.eu/resource/authority/frequency/ANNUAL</entry>
  </xsl:variable>

  <!--
  https://w3id.org/mobilitydcat-ap/update-frequency
  -->
  <xsl:variable name="mobilityDcatApUpdateFrequencyToUri"
                as="node()*">
    <entry key="1h">https://w3id.org/mobilitydcat-ap/update-frequency/1h</entry>
    <entry key="1min">https://w3id.org/mobilitydcat-ap/update-frequency/1min</entry>
    <entry key="10min">https://w3id.org/mobilitydcat-ap/update-frequency/10min</entry>
    <entry key="12h">https://w3id.org/mobilitydcat-ap/update-frequency/12h</entry>
    <entry key="15min">https://w3id.org/mobilitydcat-ap/update-frequency/15min</entry>
    <entry key="2h">https://w3id.org/mobilitydcat-ap/update-frequency/2h</entry>
    <entry key="24h">https://w3id.org/mobilitydcat-ap/update-frequency/24h</entry>
    <entry key="3h">https://w3id.org/mobilitydcat-ap/update-frequency/3h</entry>
    <entry key="3 months">https://w3id.org/mobilitydcat-ap/update-frequency/3-months</entry>
    <entry key="30min">https://w3id.org/mobilitydcat-ap/update-frequency/30min</entry>
    <entry key="5min">https://w3id.org/mobilitydcat-ap/update-frequency/5min</entry>
    <entry key="6 months">https://w3id.org/mobilitydcat-ap/update-frequency/6-months</entry>
  </xsl:variable>

  <xsl:variable name="isoDurationToMobilityDcatApUpdateFrequency"
                as="node()*">
    <!--    <entry key="X">https://w3id.org/mobilitydcat-ap/update-frequency/less-than-yearly</entry>-->
    <entry key="P0Y0M0DT1H0M0S">https://w3id.org/mobilitydcat-ap/update-frequency/1h</entry>
    <entry key="P0Y0M0DT0H1M0S">https://w3id.org/mobilitydcat-ap/update-frequency/1min</entry>
    <entry key="P0Y0M0DT0H10M0S">https://w3id.org/mobilitydcat-ap/update-frequency/10min</entry>
    <entry key="P0Y0M0DT12H0M0S">https://w3id.org/mobilitydcat-ap/update-frequency/12h</entry>
    <entry key="P0Y0M0DT0H15M0S">https://w3id.org/mobilitydcat-ap/update-frequency/15min</entry>
    <entry key="P0Y0M0DT2H0M0S">https://w3id.org/mobilitydcat-ap/update-frequency/2h</entry>
    <entry key="P0Y0M0DT24H0M0S">https://w3id.org/mobilitydcat-ap/update-frequency/24h</entry>
    <entry key="P0Y0M0DT3H0M0S">https://w3id.org/mobilitydcat-ap/update-frequency/3h</entry>
    <entry key="P0Y3M0DT0H0M0S">https://w3id.org/mobilitydcat-ap/update-frequency/3-months</entry>
    <entry key="P0Y0M0DT0H30M0S">https://w3id.org/mobilitydcat-ap/update-frequency/30min</entry>
    <entry key="P0Y0M0DT0H5M0S">https://w3id.org/mobilitydcat-ap/update-frequency/5min</entry>
    <entry key="P0Y6M0DT0H0M0S">https://w3id.org/mobilitydcat-ap/update-frequency/6-months</entry>
  </xsl:variable>


  <xsl:param name="multipleAccrualPeriodicityAllowed"
             as="xs:string"
             select="'true'"/>
  <xsl:variable name="allowMultipleAccrualPeriodicity"
                as="xs:boolean"
                select="xs:boolean($multipleAccrualPeriodicityAllowed)"/>

  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:resourceMaintenance">
    <xsl:variable name="isFirstMaintenanceElement"
                  select="preceding-sibling::*[1]/name() != 'mri:resourceMaintenance'"/>
    <xsl:if test="$isFirstMaintenanceElement">
      <xsl:variable name="listOfFrequency"
                    as="node()*">
        <xsl:for-each select="../mri:resourceMaintenance/*">
          <xsl:for-each select="mmi:maintenanceAndUpdateFrequency[*/@codeListValue != '']">
            <xsl:variable name="dcFrequency"
                          as="xs:string?"
                          select="$isoFrequencyToDublinCore[@key = current()/*/@codeListValue]"/>

            <xsl:variable name="dcatFrequencyUri"
                          as="xs:string?"
                          select="$euUpdateFrquencyToUri[@key = current()/*/@codeListValue]"/>

            <xsl:variable name="mobilityDcatApUpdateFrequencyUri"
                          as="xs:string?"
                          select="$mobilityDcatApUpdateFrequencyToUri[@key = current()/*/@codeListValue]"/>

            <!-- Domain specific codelists take priority -->
            <xsl:variable name="dcOrIsoFrequency"
                          as="xs:string?"
                          select="if ($mobilityDcatApUpdateFrequencyUri != '' or $dcatFrequencyUri != '') then ''
                                   else if($dcFrequency)
                                   then concat($europaPublicationBaseUri, 'frequency/', $dcFrequency)
                                   else concat($isoCodeListBaseUri, */@codeListValue)"/>

            <xsl:for-each select="($mobilityDcatApUpdateFrequencyUri, $dcatFrequencyUri, $dcOrIsoFrequency[. != ''])">
              <dct:accrualPeriodicity>
                <dct:Frequency rdf:about="{current()}"/>
              </dct:accrualPeriodicity>
            </xsl:for-each>
          </xsl:for-each>

          <xsl:for-each select="mmi:userDefinedMaintenanceFrequency">
            <xsl:variable name="mobilityFrequencyFromPeriod"
                          as="xs:string?"
                          select="$isoDurationToMobilityDcatApUpdateFrequency[@key = current()/gco:TM_PeriodDuration]"/>

            <xsl:if test="$mobilityFrequencyFromPeriod">
              <dct:accrualPeriodicity>
                <dct:Frequency rdf:about="{$mobilityFrequencyFromPeriod}"/>
              </dct:accrualPeriodicity>
            </xsl:if>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:variable>

      <xsl:copy-of select="if ($allowMultipleAccrualPeriodicity) then $listOfFrequency else $listOfFrequency[1]"/>
    </xsl:if>
  </xsl:template>

  <!--
  RDF Property:	dcat:temporalResolution
  Definition:	Minimum time period resolvable in the dataset.
  Range:	rdfs:Literal typed as xsd:duration
  Usage note:	If the dataset is a time-series this should correspond to the spacing of items in the series.
  For other kinds of dataset, this property will usually indicate the smallest time difference between items in the dataset.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:temporalResolution/*">
    <dcat:temporalResolution rdf:datatype="http://www.w3.org/2001/XMLSchema#duration">
      <xsl:value-of select="text()"/>
    </dcat:temporalResolution>
  </xsl:template>


  <!--
  RDF Property:	dcterms:temporal
  Definition:	The temporal period that the dataset covers.
  Range:	dcterms:PeriodOfTime (An interval of time that is named or defined by its start and end dates)
  Usage note:	The temporal coverage of a dataset may be encoded as an instance of dcterms:PeriodOfTime,
   or may be indicated using an IRI reference (link) to a resource describing a time period or interval.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:extent/*/gex:temporalElement/*/gex:extent">
    <dct:temporal>
      <dct:PeriodOfTime>
        <xsl:for-each select="*/gml:begin/*[gml:timePosition/text() != '']|*/gml:beginPosition[. != '']">
          <xsl:call-template name="rdf-date">
            <xsl:with-param name="nodeName" select="'dcat:startDate'"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="*/gml:end/*[gml:timePosition/text() != '']|*/gml:endPosition[. != '']">
          <xsl:call-template name="rdf-date">
            <xsl:with-param name="nodeName" select="'dcat:endDate'"/>
          </xsl:call-template>
        </xsl:for-each>
      </dct:PeriodOfTime>
    </dct:temporal>
  </xsl:template>


  <!--
  RDF Property:	dcterms:spatial
  Definition:	The geographical area covered by the dataset.
  Range:	dcterms:Location (A spatial region or named place)
  Usage note:	The spatial coverage of a dataset may be encoded as an instance of dcterms:Location,
  or may be indicated using an IRI reference (link) to a resource describing a location. It is recommended that links are to entries in a well maintained gazetteer such as Geonames.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:extent/*/gex:geographicElement/gex:EX_GeographicBoundingBox">
    <xsl:variable name="north" select="gex:northBoundLatitude/gco:Decimal"/>
    <xsl:variable name="east" select="gex:eastBoundLongitude/gco:Decimal"/>
    <xsl:variable name="south" select="gex:southBoundLatitude/gco:Decimal"/>
    <xsl:variable name="west" select="gex:westBoundLongitude/gco:Decimal"/>

    <xsl:variable name="geojson"
                  as="xs:string"
                  select="concat('{&quot;type&quot;:&quot;Polygon&quot;,&quot;coordinates&quot;:[[[',
                                   $west, ',', $north, '],[',
                                   $east, ',', $north, '],[',
                                   $east, ',', $south, '],[',
                                   $west, ',', $south, '],[',
                                   $west, ',', $north, ']]]}')"/>

    <dct:spatial>
      <rdf:Description>
        <rdf:type rdf:resource="http://purl.org/dc/terms/Location"/>
        <dcat:bbox rdf:datatype="http://www.opengis.net/ont/geosparql#geoJSONLiteral">
          <xsl:value-of select="$geojson"/>
        </dcat:bbox>
      </rdf:Description>
    </dct:spatial>
  </xsl:template>


  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:extent/*/gex:geographicElement/gex:EX_GeographicDescription">
    <xsl:for-each select="gex:geographicIdentifier/*">
      <xsl:variable name="uri"
                    as="xs:string?"
                    select="mcc:code/*/@xlink:href"/>
      <xsl:choose>
        <xsl:when test="string($uri)">
          <dct:spatial>
            <dct:Location rdf:about="{$uri}"/>
          </dct:spatial>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="mcc:code">
            <dct:spatial>
              <rdf:Description>
                <rdf:type rdf:resource="http://purl.org/dc/terms/Location"/>
                <xsl:call-template name="rdf-localised">
                  <xsl:with-param name="nodeName" select="'skos:prefLabel'"/>
                </xsl:call-template>
              </rdf:Description>
            </dct:spatial>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>


  <!--
  RDF Property:	prov:wasGeneratedBy
  Definition:	An activity that generated, or provides the business context for, the creation of the dataset.
  Domain:	prov:Entity
  Range:	prov:Activity An activity is something that occurs over a period of time and acts upon or with entities; it may include consuming, processing, transforming, modifying, relocating, using, or generating entities.
  Usage note:	The activity associated with generation of a dataset will typically be an initiative, project, mission, survey, on-going activity ("business as usual") etc. Multiple prov:wasGeneratedBy properties can be used to indicate the dataset production context at various levels of granularity.
  Usage note:	Use prov:qualifiedGeneration to attach additional details about the relationship between the dataset and the activity, e.g., the exact time that the dataset was produced during the lifetime of a project

  TODO
  -->

  <!--
  RDF Property:	dcat:inSeries
  Definition:	A dataset series of which the dataset is part.
  Range:	dcat:DatasetSeries
  Sub-property of:	dcterms:isPartOf

  See dcat-core-associated.xsl
  -->


</xsl:stylesheet>
