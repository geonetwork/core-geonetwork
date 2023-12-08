<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:dct="http://purl.org/dc/terms/"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                exclude-result-prefixes="#all">

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
        <xsl:comment>WARNING: Spatial resolution only supported in meters. <xsl:value-of select="concat(*/text(), ' ', */@uom)"/> is ignored (can be related to unknown unit or no conversion factor or not a decimal value).</xsl:comment>
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
  RDF Property:	dcat:temporalResolution
  Definition:	Minimum time period resolvable in the dataset.
  Range:	rdfs:Literal typed as xsd:duration
  Usage note:	If the dataset is a time-series this should correspond to the spacing of items in the series.
  For other kinds of dataset, this property will usually indicate the smallest time difference between items in the dataset.
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mri:temporalResolution/*">
    <dcat:temporalResolution rdf:datatype="http://www.w3.org/2001/XMLSchema#duration"><xsl:value-of select="text()"/></dcat:temporalResolution>
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
        <xsl:for-each select="*/gml:begin/*[gml:timePosition/text() != '']">
          <xsl:call-template name="rdf-date">
            <xsl:with-param name="nodeName" select="'dcat:startDate'"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="*/gml:end/*[gml:timePosition/text() != '']">
          <xsl:call-template name="rdf-date">
            <xsl:with-param name="nodeName" select="'dcat:endDate'"/>
          </xsl:call-template>
        </xsl:for-each>
      </dct:PeriodOfTime>
    </dct:temporal>
  </xsl:template>

</xsl:stylesheet>
