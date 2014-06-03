<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  Suggest the creation of a default topological consistency report
  when INSPIRE theme is set to Hydrography, Transport Networks or Utility and governmental services
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:geonet="http://www.fao.org/geonetwork" xmlns:gml="http://www.opengis.net/gml"
  xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:skos="http://www.w3.org/2004/02/skos/core#"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  exclude-result-prefixes="gmd">

  <xsl:import href="process-utility.xsl"/>

  <!-- i18n information -->
  <xsl:variable name="inspire-add-dq-toporeport-loc">
    <msg id="a" xml:lang="eng"> INSPIRE theme(s) found. Run this task to add a topological
      consistency section.</msg>
    <msg id="a" xml:lang="fre"> thème(s) INSPIRE trouvé(s). Exécuter cette action pour ajouter une
      section sur la cohérence topologique.</msg>
  </xsl:variable>

  <xsl:variable name="inspire-thesaurus-dq-topo"
    select="document(concat('file:///', replace(system-property(concat(substring-after($baseUrl, '/'), '.codeList.dir')), '\\', '/'), '/external/thesauri/theme/inspire-theme.rdf'))"/>

  <xsl:template name="list-inspire-add-dq-toporeport">
    <suggestion process="inspire-add-dq-toporeport"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-inspire-add-dq-toporeport">
    <xsl:param name="root"/>

    <!-- TODO : PT_FreeText ? -->
    <xsl:variable name="inspire-theme-found"
      select="count($inspire-thesaurus-dq-topo//skos:Concept[
                    skos:prefLabel = $root//gmd:keyword/gco:CharacterString and
                    (
                    @rdf:about = 'http://rdfdata.eionet.europa.eu/inspirethemes/themes/8' or
                    @rdf:about = 'http://rdfdata.eionet.europa.eu/inspirethemes/themes/7' or
                    @rdf:about = 'http://rdfdata.eionet.europa.eu/inspirethemes/themes/19'
                    )])"/>
    <xsl:message><xsl:value-of select="$inspire-th" /> </xsl:message>

    <!-- Check no topological consistency section -->
    <xsl:if test="$inspire-theme-found and count($root//gmd:DQ_TopologicalConsistency)=0">
      <suggestion process="inspire-add-dq-toporeport" category="keyword" target="keyword">
        <name>
          <xsl:value-of select="$inspire-theme-found"/>
          <xsl:value-of select="geonet:i18n($inspire-add-dq-toporeport-loc, 'a', $guiLang)"/>
        </name>
        <operational>true</operational>
        <form/>
      </suggestion>
    </xsl:if>
  </xsl:template>


  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>


  <!-- ================================================================= -->
  <!-- Add a topological consistency section  -->
  <!-- ================================================================= -->
  <xsl:template match="/gmd:MD_Metadata|/*[@gco:isoType='gmd:MD_Metadata']">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of
        select="gmd:fileIdentifier|
				gmd:language|
				gmd:characterSet|
				gmd:parentIdentifier|
				gmd:hierarchyLevel|
				gmd:hierarchyLevelName|
				gmd:contact|
				gmd:dateStamp|
				gmd:metadataStandardName|
				gmd:metadataStandardVersion|
				gmd:dataSetURI|
				gmd:locale|
				gmd:spatialRepresentationInfo|
				gmd:referenceSystemInfo|
				gmd:metadataExtensionInfo|
				gmd:identificationInfo|
				gmd:contentInfo|
				gmd:distributionInfo"/>

      <gmd:dataQualityInfo>
        <gmd:DQ_DataQuality>
          <xsl:copy-of select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:scope"/>
          <xsl:copy-of select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report"/>
          <gmd:report>
            <gmd:DQ_TopologicalConsistency>
              <gmd:nameOfMeasure gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:nameOfMeasure>
              <gmd:measureDescription gco:nilReason="missing">
                <gco:CharacterString/>
              </gmd:measureDescription>
            </gmd:DQ_TopologicalConsistency>
          </gmd:report>
          <xsl:copy-of select="gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage"/>
        </gmd:DQ_DataQuality>
      </gmd:dataQualityInfo>

      <xsl:copy-of
        select="gmd:portrayalCatalogueInfo|
				gmd:metadataConstraints|
				gmd:applicationSchemaInfo|
				gmd:metadataMaintenance|
				gmd:series|
				gmd:describes|
				gmd:propertyType|
				gmd:featureType|
				gmd:featureAttribute"
      />
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
