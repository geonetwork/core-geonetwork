<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#" exclude-result-prefixes="gmd">
  
  <xsl:import href="process-utility.xsl"/>
  
  <xsl:param name="dataDir"/>
  
  <!-- i18n information -->
  <xsl:variable name="inspire-conformity-loc">
    <msg id="a" xml:lang="en"> INSPIRE theme(s) found. Run this task to add an INSPIRE conformity section.</msg>
    <msg id="a" xml:lang="fr"> thème(s) INSPIRE trouvé(s). Exécuter cette action pour ajouter une section conformité INSPIRE.</msg>
  </xsl:variable>
  
  <!-- TODO : retrieve local copy -->
  <xsl:variable name="inspire-thesaurus"
    select="document(concat(system-property(concat(substring-after($baseUrl, '/'), '.data.dir')), '/codelist/external/thesauri/theme/inspire-theme.rdf'))"/>
  <!--<xsl:variable name="inspire-thesaurus"
    select="document('http://geonetwork.svn.sourceforge.net/svnroot/geonetwork/utilities/gemet/thesauri/inspire-theme.rdf')"/>-->
  
  <xsl:variable name="inspire-theme" select="$inspire-thesaurus//skos:Concept"/>

  <xsl:template name="list-inspire-add-conformity">
    <suggestion process="inspire-add-conformity"/>
  </xsl:template>

  <!-- Analyze the metadata record and return available suggestion
    for that process -->
  <xsl:template name="analyze-inspire-add-conformity">
    <xsl:param name="root"/>
    
    <!-- TODO : PT_FreeText ? -->
    <xsl:variable name="inspire-theme-found"
      select="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $root//gmd:keyword/gco:CharacterString])"/>
    <!-- Check no conformity -->
    <xsl:if test="$inspire-theme-found and count($root//gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title[contains(gco:CharacterString, 'INSPIRE')])=0">
      <suggestion process="inspire-add-conformity" category="keyword" target="keyword">
        <name><xsl:value-of select="$inspire-theme-found"/> <xsl:value-of select="geonet:i18n($inspire-conformity-loc, 'a', $guiLang)"/></name>
        <operational>true</operational>
        <form/>
      </suggestion>
    </xsl:if>
  </xsl:template>


  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>


  <!-- ================================================================= -->
  <!-- Add a dataQuality section to set INSPIRE conformance result     
		 Set the report date to metadata date stamp					       -->
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
				gmd:distributionInfo|
				gmd:dataQualityInfo"/>

      <gmd:dataQualityInfo>
        <gmd:DQ_DataQuality>
          <gmd:scope>
            <gmd:DQ_Scope>
              <gmd:level>
                <gmd:MD_ScopeCode codeListValue="dataset"
                  codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode"/>
              </gmd:level>
            </gmd:DQ_Scope>
          </gmd:scope>
          <gmd:report>
            <gmd:DQ_DomainConsistency>
              <gmd:result>
                <gmd:DQ_ConformanceResult>
                  <gmd:specification>
                    <gmd:CI_Citation>
                      <gmd:title>
                        <gco:CharacterString>INSPIRE Implementing rules</gco:CharacterString>
                      </gmd:title>
                      <gmd:date>
                        <gmd:CI_Date>
                          <gmd:date>
                            <gco:Date>
                              <xsl:value-of
                                select="substring-before(gmd:dateStamp/gco:DateTime, 'T')"/>
                            </gco:Date>
                          </gmd:date>
                          <gmd:dateType>
                            <gmd:CI_DateTypeCode
                              codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode"
                              codeListValue="publication"/>
                          </gmd:dateType>
                        </gmd:CI_Date>
                      </gmd:date>
                    </gmd:CI_Citation>
                  </gmd:specification>
                  <gmd:explanation>
                    <gco:CharacterString>-- More information on the test --</gco:CharacterString>
                  </gmd:explanation>
                  <gmd:pass>
                    <gco:Boolean>1</gco:Boolean>
                  </gmd:pass>
                </gmd:DQ_ConformanceResult>
              </gmd:result>
            </gmd:DQ_DomainConsistency>
          </gmd:report>
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
