<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:geonet="http://www.fao.org/geonetwork"
  xmlns:gml="http://www.opengis.net/gml" xmlns:srv="http://www.isotc211.org/2005/srv"
  xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd"
  xmlns:skos="http://www.w3.org/2004/02/skos/core#" exclude-result-prefixes="gmd">
  
  <xsl:import href="process-utility.xsl"/>
  
  <xsl:variable name="dateFormat">[Y0001]-[M01]-[D01]</xsl:variable>
  
  <xsl:param name="dataDir"/>
  
  <!-- i18n information -->
  <xsl:variable name="inspire-conformity-loc">
    <msg id="a" xml:lang="eng"> INSPIRE theme(s) found. Run this task to add an INSPIRE conformity section.</msg>
    <msg id="a" xml:lang="fre"> thème(s) INSPIRE trouvé(s). Exécuter cette action pour ajouter une section conformité INSPIRE.</msg>
  </xsl:variable>
  
  
  <!--
        Mapping between INSPIRE Themes from annex I and Data sepcification title
        -->
  <xsl:variable name="specificationTitles">
    <spec theme="Administrative Units" title="INSPIRE Data Specification on Administrative Units - Guidelines v3.0.1" date="2010-05-03"/>
    <spec theme="Cadastral Parcels" title="INSPIRE Data Specification on Cadastral Parcels - Guidelines v 3.0.1" date="2010-05-03"/>
    <spec theme="Geographical Names" title="INSPIRE Data Specification on Geographical Names - Guidelines v 3.0.1" date="2010-05-03"/>
    <spec theme="Hydrography" title="INSPIRE Data Specification on Hydrography - Guidelines v 3.0.1" date="2010-05-03"/>
    <spec theme="Protected Sites" title="INSPIRE Data Specification on Protected Sites - Guidelines v 3.1.0" date="2010-05-03"/>
    <spec theme="Transport Networks" title="INSPIRE Data Specification on Transport Networks - Guidelines v 3.1.0" date="2010-05-03"/>
    <spec theme="Addresses" title="INSPIRE Data Specification on Addresses - Guidelines v 3.0.1" date="2010-05-03"/>
    <spec theme="Coordinate Reference Systems" title="INSPIRE Specification on Coordinate Reference Systems - Guidelines v 3.1" date="2010-05-03"/>
    <spec theme="Geographical Grid Systems" title="INSPIRE Specification on Geographical Grid Systems - Guidelines v 3.0.1" date="2010-05-03"/>
    <spec title="COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services" date="2010-10-23"/>
  </xsl:variable>
  
  <!-- TODO : retrieve local copy -->
  <xsl:variable name="inspire-thesaurus"
    select="document(concat('file:///', replace(system-property(concat(substring-after($baseUrl, '/'), '.codeList.dir')), '\\', '/'), '/external/thesauri/theme/inspire-theme.rdf'))"/>
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


      <!-- Add one data quality report per themes from Annex I -->      
      <xsl:variable name="keywords" select="//gmd:keyword/gco:CharacterString"/>
      <xsl:variable name="metadataInspireThemes" select="$inspire-thesaurus//skos:Concept[skos:prefLabel = $keywords]/skos:prefLabel[@xml:lang='en']"/>
      <xsl:variable name="titles" select="$specificationTitles/spec[@theme = $metadataInspireThemes]"/>

      <xsl:choose>
        <xsl:when test="$titles">
          <xsl:for-each select="$titles">
            <xsl:call-template name="generateDataQualityReport">
              <xsl:with-param name="title" select="@title"/>
              <xsl:with-param name="date" select="if (@date) then @date else format-dateTime(current-dateTime(),$dateFormat)"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="generateDataQualityReport">
            <xsl:with-param name="title" select="'INSPIRE Implementing rules'"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
      
      
      <!-- For Annex II and III and reference to commission regulation 1089/2010 -->
      <xsl:variable name="isThemesFromAnnexIIorIII">
        <xsl:for-each select="$metadataInspireThemes">
          <xsl:if test=".!='coordinate reference systems' and .!='geographical grid systems' 
            and .!='geographical names' and .!='administrative units' 
            and .!='addresses' and .!='cadastral parcels' 
            and .!='transport networks' and .!='hydrography' 
            and .!='protected sites'"><xsl:value-of select="true()"/></xsl:if>
        </xsl:for-each>
      </xsl:variable>
      
      <xsl:if test="$isThemesFromAnnexIIorIII">
        <xsl:call-template name="generateDataQualityReport">
          <xsl:with-param name="title" select="$specificationTitles/spec[not(@theme)]/@title"/>
          <xsl:with-param name="date" select="if ($specificationTitles/spec[not(@theme)]/@date) 
                                              then $specificationTitles/spec[not(@theme)]/@date
                                              else format-dateTime(current-dateTime(),$dateFormat)"/>
        </xsl:call-template>
      </xsl:if>
      
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

  <xsl:template name="generateDataQualityReport">
    <xsl:param name="title"/>
    <xsl:param name="pass" select="'0'"/>
    <xsl:param name="date" select="format-dateTime(current-dateTime(),$dateFormat)"/>
    
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
                      <gco:CharacterString><xsl:value-of select="$title"/></gco:CharacterString>
                    </gmd:title>
                    <gmd:date>
                      <gmd:CI_Date>
                        <gmd:date>
                          <gco:Date>
                            <xsl:value-of
                              select="$date"/>
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
                  <gco:CharacterString>See the referenced specification</gco:CharacterString>
                </gmd:explanation>
                <gmd:pass>
                  <gco:Boolean><xsl:value-of select="$pass"/></gco:Boolean>
                </gmd:pass>
              </gmd:DQ_ConformanceResult>
            </gmd:result>
          </gmd:DQ_DomainConsistency>
        </gmd:report>
      </gmd:DQ_DataQuality>
    </gmd:dataQualityInfo>
  </xsl:template>

  <!-- ================================================================= -->

</xsl:stylesheet>
