<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright (C) 2001-2016 Food and Agriculture Organization of the
  ~ United Nations (FAO-UN), United Nations World Food Programme (WFP)
  ~ and United Nations Environment Programme (UNEP)
  ~
  ~ This program is free software; you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation; either version 2 of the License, or (at
  ~ your option) any later version.
  ~
  ~ This program is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program; if not, write to the Free Software
  ~ Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
  ~
  ~ Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
  ~ Rome - Italy. email: geonetwork@osgeo.org
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:import href="process-utility.xsl"/>

  <xsl:variable name="dateFormat">[Y0001]-[M01]-[D01]</xsl:variable>

  <xsl:param name="dataDir"/>

  <!-- i18n information -->
  <xsl:variable name="inspire-conformity-loc">
    <msg id="a" xml:lang="eng">INSPIRE theme(s) found. Run this task to add an INSPIRE conformity
      section.
    </msg>
    <msg id="a" xml:lang="fre">thème(s) INSPIRE trouvé(s). Exécuter cette action pour ajouter une
      section conformité INSPIRE.
    </msg>
    <msg id="a" xml:lang="dut">INSPIRE-thema(s) gevonden. Voer deze taak uit om de INSPIRE-conformiteit sectie toe te voegen.
    </msg>
  </xsl:variable>


  <!--
        Mapping between INSPIRE Themes from annex I and Data sepcification title
        -->
  <xsl:variable name="specificationTitles">
    <eng>
      <spec theme="Administrative Units"
            title="INSPIRE Data Specification on Administrative Units - Guidelines v3.0.1"
            date="2010-05-03"/>
      <spec theme="Cadastral Parcels"
            title="INSPIRE Data Specification on Cadastral Parcels - Guidelines v 3.0.1"
            date="2010-05-03"/>
      <spec theme="Geographical Names"
            title="INSPIRE Data Specification on Geographical Names - Guidelines v 3.0.1"
            date="2010-05-03"/>
      <spec theme="Hydrography"
            title="INSPIRE Data Specification on Hydrography - Guidelines v 3.0.1"
            date="2010-05-03"/>
      <spec theme="Protected Sites"
            title="INSPIRE Data Specification on Protected Sites - Guidelines v 3.1.0"
            date="2010-05-03"/>
      <spec theme="Transport Networks"
            title="INSPIRE Data Specification on Transport Networks - Guidelines v 3.1.0"
            date="2010-05-03"/>
      <spec theme="Addresses" title="INSPIRE Data Specification on Addresses - Guidelines v 3.0.1"
            date="2010-05-03"/>
      <spec theme="Coordinate Reference Systems"
            title="INSPIRE Specification on Coordinate Reference Systems - Guidelines v 3.1"
            date="2010-05-03"/>
      <spec theme="Geographical Grid Systems"
            title="INSPIRE Specification on Geographical Grid Systems - Guidelines v 3.0.1"
            date="2010-05-03"/>
      <spec
        title="COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services"
        date="2010-12-08"/>
    </eng>
    <fre>
      <spec theme="Administrative Units"
            title="Guide INSPIRE sur les unités administratives - v3.0.1" date="2010-05-03"/>
      <spec theme="Cadastral Parcels" title="Guide INSPIRE sur les parcelles cadastrales - v 3.0.1"
            date="2010-05-03"/>
      <spec theme="Geographical Names"
            title="Guide INSPIRE sur les dénominations géographiques - v 3.0.1" date="2010-05-03"/>
      <spec theme="Hydrography" title="Guide INSPIRE sur l’hydrographie - v 3.0.1"
            date="2010-05-03"/>
      <spec theme="Protected Sites" title="Guide INSPIRE sur les sites protégés - v 3.1.0"
            date="2010-05-03"/>
      <spec theme="Transport Networks" title="Guide INSPIRE sur les réseaux de transport - v 3.1.0"
            date="2010-05-03"/>
      <spec theme="Addresses" title="Guide INSPIRE sur les adresses - v 3.0.1" date="2010-05-03"/>
      <spec theme="Coordinate Reference Systems"
            title="Guide INSPIRE sur les référentiels de coordonnées - v 3.1" date="2010-05-03"/>
      <spec theme="Geographical Grid Systems"
            title="Guide INSPIRE sur les systèmes de maillage géographique - v 3.0.1"
            date="2010-05-03"/>
      <spec
        title="RÈGLEMENT (UE) N o 1089/2010 DE LA COMMISSION du 23 novembre 2010 portant modalités d'application de la directive 2007/2/CE du Parlement européen et du Conseil en ce qui concerne l'interopérabilité des séries et des services de données géographiques"
        date="2010-12-08"/>
    </fre>
  </xsl:variable>

  <!-- TODO : retrieve local copy -->
  <xsl:variable name="inspire-thesaurus"
                select="document(concat('file:///', replace(util:getConfigValue('codeListDir'), '\\', '/'), '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf'))"/>

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
    <xsl:if
      test="$inspire-theme-found and count($root//gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title[contains(gco:CharacterString, 'INSPIRE')])=0">
      <suggestion process="inspire-add-conformity" category="keyword" target="keyword">
        <name>
          <xsl:value-of select="$inspire-theme-found"/>
          <xsl:value-of select="geonet:i18n($inspire-conformity-loc, 'a', $guiLang)"/>
        </name>
        <operational>true</operational>
        <form/>
      </suggestion>
    </xsl:if>
  </xsl:template>


  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>


  <!-- ================================================================= -->
  <!-- Add a dataQuality section to set INSPIRE conformance result
     Set the report date to metadata date stamp                 -->
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
      <xsl:variable name="keywords"
                    select="//gmd:keyword/gco:CharacterString"/>
      <xsl:variable name="metadataLanguage"
                    select="//gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>
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
          $theme != 'protected sites'">
            <xsl:value-of select="true()"/>
          </xsl:if>
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
                      <gco:CharacterString>
                        <xsl:value-of select="$title"/>
                      </gco:CharacterString>
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
                  <gco:Boolean>
                    <xsl:value-of select="$pass"/>
                  </gco:Boolean>
                </gmd:pass>
              </gmd:DQ_ConformanceResult>
            </gmd:result>
          </gmd:DQ_DomainConsistency>
        </gmd:report>
      </gmd:DQ_DataQuality>
    </gmd:dataQualityInfo>
  </xsl:template>
</xsl:stylesheet>
