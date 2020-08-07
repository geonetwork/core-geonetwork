<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                exclude-result-prefixes="#all">
  
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
    <eng>
      <spec theme="Administrative Units" title="INSPIRE Data Specification on Administrative Units - Guidelines v3.0.1" date="2010-05-03"/>
      <spec theme="Cadastral Parcels" title="INSPIRE Data Specification on Cadastral Parcels - Guidelines v 3.0.1" date="2010-05-03"/>
      <spec theme="Geographical Names" title="INSPIRE Data Specification on Geographical Names - Guidelines v 3.0.1" date="2010-05-03"/>
      <spec theme="Hydrography" title="INSPIRE Data Specification on Hydrography - Guidelines v 3.0.1" date="2010-05-03"/>
      <spec theme="Protected Sites" title="INSPIRE Data Specification on Protected Sites - Guidelines v 3.1.0" date="2010-05-03"/>
      <spec theme="Transport Networks" title="INSPIRE Data Specification on Transport Networks - Guidelines v 3.1.0" date="2010-05-03"/>
      <spec theme="Addresses" title="INSPIRE Data Specification on Addresses - Guidelines v 3.0.1" date="2010-05-03"/>
      <spec theme="Coordinate Reference Systems" title="INSPIRE Specification on Coordinate Reference Systems - Guidelines v 3.1" date="2010-05-03"/>
      <spec theme="Geographical Grid Systems" title="INSPIRE Specification on Geographical Grid Systems - Guidelines v 3.0.1" date="2010-05-03"/>
      <spec title="COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services" date="2010-12-08"/>
    </eng>
    <fre>
      <spec theme="Administrative Units" title="Guide INSPIRE sur les unités administratives - v3.0.1" date="2010-05-03"/>
      <spec theme="Cadastral Parcels" title="Guide INSPIRE sur les parcelles cadastrales - v 3.0.1" date="2010-05-03"/>
      <spec theme="Geographical Names" title="Guide INSPIRE sur les dénominations géographiques - v 3.0.1" date="2010-05-03"/>
      <spec theme="Hydrography" title="Guide INSPIRE sur l’hydrographie - v 3.0.1" date="2010-05-03"/>
      <spec theme="Protected Sites" title="Guide INSPIRE sur les sites protégés - v 3.1.0" date="2010-05-03"/>
      <spec theme="Transport Networks" title="Guide INSPIRE sur les réseaux de transport - v 3.1.0" date="2010-05-03"/>
      <spec theme="Addresses" title="Guide INSPIRE sur les adresses - v 3.0.1" date="2010-05-03"/>
      <spec theme="Coordinate Reference Systems" title="Guide INSPIRE sur les référentiels de coordonnées - v 3.1" date="2010-05-03"/>
      <spec theme="Geographical Grid Systems" title="Guide INSPIRE sur les systèmes de maillage géographique - v 3.0.1" date="2010-05-03"/>
      <spec title="RÈGLEMENT (UE) N o 1089/2010 DE LA COMMISSION du 23 novembre 2010 portant modalités d'application de la directive 2007/2/CE du Parlement européen et du Conseil en ce qui concerne l'interopérabilité des séries et des services de données géographiques" date="2010-12-08"/>
    </fre>
  </xsl:variable>
  
  <xsl:variable name="inspire-thesaurus"
    select="document(concat('file:///', replace(util:getConfigValue('codeListDir'), '\\', '/'), '/external/thesauri/theme/inspire-theme.rdf'))"/>

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
      select="count($inspire-thesaurus//skos:Concept[skos:prefLabel = $root//mri:keyword/gco:CharacterString])"/>
    <!-- Check no conformity -->
    <xsl:if test="$inspire-theme-found and
                  count($root//mdq:DQ_DomainConsistency/mdq:result/mdq:DQ_ConformanceResult/mdq:specification/cit:CI_Citation/cit:title[contains(gco:CharacterString, 'INSPIRE')]) = 0">
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
  <xsl:template match="/mdb:MD_Metadata">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="mdb:metadataIdentifier|
                            mdb:defaultLocale|
                            mdb:parentMetadata|
                            mdb:metadataScope|
                            mdb:contact|
                            mdb:dateInfo|
                            mdb:metadataStandard|
                            mdb:metadataProfile|
                            mdb:alternativeMetadataReference|
                            mdb:otherLocale|
                            mdb:metadataLinkage|
                            mdb:spatialRepresentationInfo|
                            mdb:referenceSystemInfo|
                            mdb:metadataExtensionInfo|
                            mdb:identificationInfo|
                            mdb:contentInfo|
                            mdb:distributionInfo|
                            mdb:dataQualityInfo"/>


      <!-- Add one data quality report per themes from Annex I -->      
      <xsl:variable name="keywords"
                    select="//mri:keyword/gco:CharacterString"/>
      <xsl:variable name="metadataLanguage"
                    select="//mdb:MD_Metadata/mdb:defaultLocale/
                              lan:PT_Locale/lan:language/lan:LanguageCode/@codeListValue"/>
      <xsl:variable name="metadataInspireThemes"
                    select="$inspire-thesaurus//skos:Concept[skos:prefLabel = $keywords]/
                                skos:prefLabel[@xml:lang='en']"/>
      <xsl:variable name="defaultTitles"
                    select="$specificationTitles/eng/
                                spec[@theme = $metadataInspireThemes/text()]"/>
      <xsl:choose>
        <xsl:when test="$metadataInspireThemes">
          <xsl:for-each select="$metadataInspireThemes">
            <xsl:variable name="current" select="."/>
            <xsl:variable name="defaultTitle"
                          select="$specificationTitles/eng/
                                spec[lower-case(@theme) = lower-case($current)]/@title"/>
            <xsl:variable name="title"
                          select="$specificationTitles/*[name() = $metadataLanguage]/
                                spec[lower-case(@theme) = lower-case($current)]/@title"/>
            <xsl:if test="$defaultTitle">
              <xsl:variable name="date"
                            select="$specificationTitles/eng/
                                spec[lower-case(@theme) = lower-case($current)]/@date"/>
              <xsl:call-template name="generateDataQualityReport">
                <xsl:with-param name="title"
                                select="if (normalize-space(title) = '')
                                        then $defaultTitle
                                        else $title"/>
                <xsl:with-param name="date"
                                select="if ($date)
                                        then $date
                                        else format-dateTime(current-dateTime(),$dateFormat)"/>
              </xsl:call-template>
            </xsl:if>
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
          <xsl:variable name="theme" select="lower-case(.)"/>
          <xsl:if test="$theme != 'coordinate reference systems' and
          $theme != 'geographical grid systems' and
          $theme != 'geographical names' and
          $theme !='administrative units' and
          $theme != 'addresses' and
          $theme !='cadastral parcels' and
          $theme != 'transport networks' and
          $theme !='hydrography' and
          $theme != 'protected sites'"><xsl:value-of select="true()"/></xsl:if>
        </xsl:for-each>
      </xsl:variable>
      
      <xsl:if test="$isThemesFromAnnexIIorIII">
        <xsl:variable name="defaultTitle"
                        select="$specificationTitles/eng/
                                    spec[not(@theme)]/@title"/>
        <xsl:variable name="title"
                        select="$specificationTitles/*[name() = $metadataLanguage]/
                                    spec[not(@theme)]/@title"/>

        <xsl:call-template name="generateDataQualityReport">
          <xsl:with-param name="title"
                          select="if (normalize-space($title) = '')
                                  then $defaultTitle
                                  else $title"/>
          <xsl:with-param name="date"
                          select="if ($specificationTitles/eng/spec[not(@theme)]/@date)
                                  then $specificationTitles/eng/spec[not(@theme)]/@date
                                  else format-dateTime(current-dateTime(),$dateFormat)"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:copy-of
              select="mdb:resourceLineage|
                      mdb:portrayalCatalogueInfo|
                      mdb:metadataConstraints|
                      mdb:applicationSchemaInfo|
                      mdb:metadataMaintenance|
                      mdb:acquisitionInformation"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="generateDataQualityReport">
    <xsl:param name="title"/>
    <xsl:param name="pass" select="'0'"/>
    <xsl:param name="date" select="format-dateTime(current-dateTime(),$dateFormat)"/>
    
    <mdb:dataQualityInfo>
      <mdq:DQ_DataQuality>
        <mdq:scope>
          <mcc:MD_Scope>
            <mcc:level>
              <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19139/resources/gmxCodelists.xml#MD_ScopeCode"
                                codeListValue="dataset"/>
            </mcc:level>
          </mcc:MD_Scope>
        </mdq:scope>
        <mdq:report>
          <mdq:DQ_DomainConsistency>
            <mdq:result>
              <mdq:DQ_ConformanceResult>
                <mdq:specification>
                  <cit:CI_Citation>
                    <cit:title>
                      <gco:CharacterString><xsl:value-of select="$title"/></gco:CharacterString>
                    </cit:title>
                    <cit:date>
                      <cit:CI_Date>
                        <cit:date>
                          <gco:Date>
                            <xsl:value-of
                              select="$date"/>
                          </gco:Date>
                        </cit:date>
                        <cit:dateType>
                          <cit:CI_DateTypeCode
                            codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode"
                            codeListValue="publication"/>
                        </cit:dateType>
                      </cit:CI_Date>
                    </cit:date>
                  </cit:CI_Citation>
                </mdq:specification>
                <mdq:explanation>
                  <gco:CharacterString>See the referenced specification</gco:CharacterString>
                </mdq:explanation>
                <mdq:pass>
                  <gco:Boolean><xsl:value-of select="$pass"/></gco:Boolean>
                </mdq:pass>
              </mdq:DQ_ConformanceResult>
            </mdq:result>
          </mdq:DQ_DomainConsistency>
        </mdq:report>
      </mdq:DQ_DataQuality>
    </mdb:dataQualityInfo>
  </xsl:template>
</xsl:stylesheet>
