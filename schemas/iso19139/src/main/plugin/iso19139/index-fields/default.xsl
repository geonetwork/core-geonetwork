<?xml version="1.0" encoding="UTF-8" ?>
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

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="../convert/functions.xsl"/>
  <xsl:include href="../../../xsl/utils-fn.xsl"/>
  <xsl:include href="inspire-util.xsl" />

  <!-- This file defines what parts of the metadata are indexed by Lucene
       Searches can be conducted on indexes defined here.
       The Field@name attribute defines the name of the search variable.
       If a variable has to be maintained in the user session, it needs to be
       added to the GeoNetwork constants in the Java source code.
       Please keep indexes consistent among metadata standards if they should
       work across different metadata resources -->
  <!-- ========================================================================================= -->

  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"/>


  <!-- ========================================================================================= -->

  <xsl:param name="thesauriDir"/>
  <xsl:param name="inspire">false</xsl:param>

  <xsl:variable name="inspire-thesaurus"
                select="if ($inspire!='false') then document(concat('file:///', replace($thesauriDir, '\\', '/'), '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf')) else ''"/>
  <xsl:variable name="inspire-theme"
                select="if ($inspire!='false') then $inspire-thesaurus//skos:Concept else ''"/>

  <!-- If identification creation, publication and revision date
    should be indexed as a temporal extent information (eg. in INSPIRE
    metadata implementing rules, those elements are defined as part
    of the description of the temporal extent). -->
  <xsl:variable name="useDateAsTemporalExtent" select="false()"/>

  <!-- Define the way keyword and thesaurus are indexed. If false
  only keyword, thesaurusName and thesaurusType field are created.
  If true, advanced field are created to make more details query
  on keyword type and search by thesaurus. Index size is bigger
  but more detailed facet can be configured based on each thesaurus.
  -->
  <xsl:variable name="indexAllKeywordDetails" select="true()"/>

  <!-- For record not having status obsolete, flag them as non
  obsolete records. Some catalog like to restrict to non obsolete
  records only the default search. -->
  <xsl:variable name="flagNonObseleteRecords" select="false()"/>

  <!-- Choose if WMS should be also indexed
  as a KML layers to be loaded in GoogleEarth -->
  <xsl:variable name="indexWmsAsKml" select="false()"/>


  <!-- The main metadata language -->
  <xsl:variable name="isoLangId">
    <xsl:call-template name="langId19139"/>
  </xsl:variable>

  <!-- ========================================================================================= -->
  <xsl:template match="/">
    <Document locale="{$isoLangId}">
      <Field name="_locale" string="{$isoLangId}" store="true" index="true"/>

      <Field name="_docLocale" string="{$isoLangId}" store="true" index="true"/>

      <xsl:variable name="_defaultTitle">
        <xsl:call-template name="defaultTitle">
          <xsl:with-param name="isoDocLangId" select="$isoLangId"/>
        </xsl:call-template>
      </xsl:variable>

      <Field name="_defaultTitle" string="{string($_defaultTitle)}" store="true" index="true"/>

      <!-- not tokenized title for sorting, needed for multilingual sorting -->
      <xsl:if test="geonet:info/isTemplate != 's'">
        <Field name="_title" string="{string($_defaultTitle)}" store="true" index="true"/>
      </xsl:if>


      <xsl:variable name="_defaultAbstract">
        <xsl:call-template name="defaultAbstract">
          <xsl:with-param name="isoDocLangId" select="$isoLangId"/>
        </xsl:call-template>
      </xsl:variable>

      <Field name="_defaultAbstract"
             string="{string($_defaultAbstract)}"
             store="true"
             index="true"/>


      <xsl:apply-templates select="*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']"
                           mode="metadata"/>

      <xsl:apply-templates mode="index" select="*"/>

    </Document>
  </xsl:template>


  <!-- Add index mode template in order to easily add new field in the index (eg. in profiles).

  For example, index some keywords from a specific thesaurus in a new field:
  <xsl:template mode="index"
      match="gmd:MD_Keywords[gmd:thesaurusName/gmd:CI_Citation/
                  gmd:title/gco:CharacterString='My thesaurus']/
                  gmd:keyword[normalize-space(gco:CharacterString) != '']">
      <Field name="myThesaurusKeyword" string="{string(.)}" store="true" index="true"/>
  </xsl:template>

  Note: if more than one template match the same element in a mode, only one will be
  used (usually the last one).

  If matching a upper level element, apply mode to its child to further index deeper level if required:
      <xsl:template mode="index" match="gmd:EX_Extent">
          ... do something
          ... and continue indexing
          <xsl:apply-templates mode="index" select="*"/>
      </xsl:template>
  -->
  <xsl:template mode="index" match="*|@*">
    <xsl:apply-templates mode="index" select="*|@*"/>
  </xsl:template>


  <xsl:template mode="index"
                match="gmd:extent/gmd:EX_Extent/gmd:description/gco:CharacterString[normalize-space(.) != '']">
    <Field name="extentDesc" string="{string(.)}" store="false" index="true"/>
  </xsl:template>


  <!-- ========================================================================================= -->

  <xsl:template match="*" mode="metadata">

    <!-- === Data or Service Identification === -->

    <!-- the double // here seems needed to index MD_DataIdentification when
         it is nested in a SV_ServiceIdentification class -->

    <xsl:for-each select="gmd:identificationInfo//gmd:MD_DataIdentification|
                gmd:identificationInfo//*[contains(@gco:isoType, 'MD_DataIdentification')]|
                gmd:identificationInfo/srv:SV_ServiceIdentification">

      <xsl:for-each select="gmd:citation/gmd:CI_Citation">
        <xsl:for-each select="gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString|gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor">
          <Field name="identifier" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:identifier/gmd:RS_Identifier/gmd:code/gco:CharacterString|gmd:identifier/gmd:RS_Identifier/gmd:code/gmx:Anchor">
          <Field name="identifier" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:identifier/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString">
        <!-- For local atom feed services -->
          <Field name="identifierNamespace" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:title/gco:CharacterString">
          <Field name="title" string="{string(.)}" store="true" index="true"/>
          <!-- not tokenized title for sorting -->
          <Field name="_title" string="{string(.)}" store="false" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:alternateTitle/gco:CharacterString">
          <Field name="altTitle" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='revision']/gmd:date">
          <Field name="revisionDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                 store="true" index="true"/>
          <Field name="createDateMonth"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 8)}" store="true"
                 index="true"/>
          <Field name="createDateYear"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 5)}" store="true"
                 index="true"/>
          <xsl:if test="$useDateAsTemporalExtent">
            <Field name="tempExtentBegin" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                   store="true" index="true"/>
          </xsl:if>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='creation']/gmd:date">
          <Field name="createDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                 store="true" index="true"/>
          <Field name="createDateMonth"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 8)}" store="true"
                 index="true"/>
          <Field name="createDateYear"
                 string="{substring(gco:Date[.!='']|gco:DateTime[.!=''], 0, 5)}" store="true"
                 index="true"/>
          <xsl:if test="$useDateAsTemporalExtent">
            <Field name="tempExtentBegin" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                   store="true" index="true"/>
          </xsl:if>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue='publication']/gmd:date">
          <Field name="publicationDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                 store="true" index="true"/>
          <xsl:if test="$useDateAsTemporalExtent">
            <Field name="tempExtentBegin" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
                   store="true" index="true"/>
          </xsl:if>
        </xsl:for-each>

        <!-- fields used to search for metadata in paper or digital format -->

        <xsl:for-each select="gmd:presentationForm">
          <xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Digital')">
            <Field name="digital" string="true" store="false" index="true"/>
          </xsl:if>

          <xsl:if test="contains(gmd:CI_PresentationFormCode/@codeListValue, 'Hardcopy')">
            <Field name="paper" string="true" store="false" index="true"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:pointOfContact[1]/*/gmd:role/*/@codeListValue">
        <Field name="responsiblePartyRole" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:abstract/gco:CharacterString">
        <Field name="abstract" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="gmd:credit/gco:CharacterString">
        <Field name="credit" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>


      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="*/gmd:EX_Extent">
        <xsl:apply-templates select="gmd:geographicElement/gmd:EX_GeographicBoundingBox"
                             mode="latLon"/>

        <xsl:for-each select="gmd:description/gco:CharacterString[normalize-space(.) != '']">
          <Field name="extentDesc" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each
          select="gmd:geographicElement/gmd:EX_GeographicDescription/gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString">
          <Field name="geoDescCode" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:temporalElement/
                                  (gmd:EX_TemporalExtent|gmd:EX_SpatialTemporalExtent)/gmd:extent">
          <xsl:for-each select="gml:TimePeriod|gml320:TimePeriod">

            <xsl:variable name="times">
              <xsl:call-template name="newGmlTime">
                <xsl:with-param name="begin"
                                select="gml:beginPosition|gml:begin/gml:TimeInstant/gml:timePosition|
                                        gml320:beginPosition|gml320:begin/gml320:TimeInstant/gml320:timePosition"/>
                <xsl:with-param name="end"
                                select="gml:endPosition|gml:end/gml:TimeInstant/gml:timePosition|
                                        gml320:endPosition|gml320:end/gml320:TimeInstant/gml320:timePosition"/>
              </xsl:call-template>
            </xsl:variable>

            <Field name="tempExtentBegin" string="{lower-case(substring-before($times,'|'))}"
                   store="true" index="true"/>
            <Field name="tempExtentEnd" string="{lower-case(substring-after($times,'|'))}"
                   store="true" index="true"/>
          </xsl:for-each>

        </xsl:for-each>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:if test="*/gmd:EX_Extent/*/gmd:EX_BoundingPolygon">
        <Field name="boundingPolygon" string="y" store="true" index="false"/>
      </xsl:if>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="//gmd:MD_Keywords">
        <!-- Index all keywords as text or anchor -->
        <xsl:variable name="listOfKeywords"
                      select="gmd:keyword/gco:CharacterString|
                                        gmd:keyword/gmx:Anchor"/>
        <xsl:variable name="thesaurusName" select="gmd:thesaurusName/gmd:CI_Citation/gmd:title/*[1]"/>

        <xsl:for-each select="$listOfKeywords">
          <xsl:variable name="keyword" select="string(.)"/>

          <Field name="keyword" string="{$keyword}" store="true" index="true"/>

          <!-- If INSPIRE is enabled, check if the keyword is one of the 34 themes
               and index annex, theme and theme in english. -->
          <xsl:if test="$inspire='true' and normalize-space(lower-case($thesaurusName)) = 'gemet - inspire themes, version 1.0'">

            <xsl:if test="string-length(.) &gt; 0">

              <xsl:variable name="inspireannex">
                <xsl:call-template name="determineInspireAnnex">
                  <xsl:with-param name="keyword" select="$keyword"/>
                  <xsl:with-param name="inspireThemes" select="$inspire-theme"/>
                </xsl:call-template>
              </xsl:variable>

              <xsl:variable name="inspireThemeAcronym">
                <xsl:call-template name="getInspireThemeAcronym">
                  <xsl:with-param name="keyword" select="$keyword"/>
                </xsl:call-template>
              </xsl:variable>

              <!-- Add the inspire field if it's one of the 34 themes -->
              <xsl:if test="normalize-space($inspireannex)!=''">
                <Field name="inspiretheme" string="{$keyword}" store="true" index="true"/>
                <Field name="inspirethemewithac"
                       string="{concat($inspireThemeAcronym, '|', $keyword)}"
                       store="true" index="true"/>

                <!--<Field name="inspirethemeacronym" string="{$inspireThemeAcronym}" store="true" index="true"/>-->
                <xsl:variable name="inspireThemeURI"
                              select="$inspire-theme[skos:prefLabel = $keyword]/@rdf:about"/>
                <Field name="inspirethemeuri" string="{$inspireThemeURI}" store="true"
                       index="true"/>

                <xsl:variable name="englishInspireTheme">
                  <xsl:call-template name="translateInspireThemeToEnglish">
                    <xsl:with-param name="keyword" select="$keyword"/>
                    <xsl:with-param name="inspireThemes" select="$inspire-theme"/>
                  </xsl:call-template>
                </xsl:variable>

                <Field name="inspiretheme_en" string="{$englishInspireTheme}" store="true"
                       index="true"/>
                <Field name="inspireannex" string="{$inspireannex}" store="true" index="true"/>
                <!-- FIXME : inspirecat field will be set multiple time if one record has many themes -->
                <Field name="inspirecat" string="true" store="false" index="true"/>
              </xsl:if>
            </xsl:if>
          </xsl:if>
        </xsl:for-each>

        <!-- Index thesaurus name to easily search for records
        using keyword from a thesaurus. -->
        <xsl:for-each select="gmd:thesaurusName/gmd:CI_Citation">
          <xsl:variable name="thesaurusIdentifier"
                        select="gmd:identifier/gmd:MD_Identifier/gmd:code/gmx:Anchor/text()"/>

          <xsl:if test="$thesaurusIdentifier != ''">
            <Field name="thesaurusIdentifier"
                   string="{substring-after($thesaurusIdentifier,'geonetwork.thesaurus.')}"
                   store="true" index="true"/>
          </xsl:if>
          <xsl:if test="gmd:title/gco:CharacterString/text() != ''">
            <Field name="thesaurusName"
                   string="{gmd:title/gco:CharacterString/text()}"
                   store="true" index="true"/>
          </xsl:if>


          <xsl:if test="$indexAllKeywordDetails and $thesaurusIdentifier != ''">
            <!-- field thesaurus-{{thesaurusIdentifier}}={{keyword}} allows
            to group all keywords of same thesaurus in a field -->
            <xsl:variable name="currentType" select="string(.)"/>

            <xsl:for-each select="$listOfKeywords">
              <Field
                name="thesaurus-{substring-after($thesaurusIdentifier,'geonetwork.thesaurus.')}"
                string="{string(.)}"
                store="true" index="true"/>

            </xsl:for-each>
          </xsl:if>
        </xsl:for-each>

        <!-- Index thesaurus type -->
        <xsl:for-each select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue">
          <Field name="keywordType" string="{string(.)}" store="true" index="true"/>
          <xsl:if test="$indexAllKeywordDetails">
            <!-- field thesaurusType{{type}}={{keyword}} allows
            to group all keywords of same type in a field -->
            <xsl:variable name="currentType" select="string(.)"/>
            <xsl:for-each select="$listOfKeywords">
              <Field name="keywordType-{$currentType}"
                     string="{string(.)}"
                     store="true" index="true"/>
            </xsl:for-each>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:variable name="listOfKeywords">{
        <xsl:variable name="keywordWithNoThesaurus"
                      select="//gmd:MD_Keywords[
                                not(gmd:thesaurusName) or gmd:thesaurusName/*/gmd:title/*/text() = '']/
                                  gmd:keyword[*/text() != '']"/>
        <xsl:for-each-group select="//gmd:MD_Keywords[gmd:thesaurusName/*/gmd:title/*/text() != '']"
                            group-by="gmd:thesaurusName/*/gmd:title/*/text()">
          '<xsl:value-of select="replace(current-grouping-key(), '''', '\\''')"/>' :[
          <xsl:for-each select="current-group()/gmd:keyword/(gco:CharacterString|gmx:Anchor)">
            {'value': <xsl:value-of select="concat('''', replace(., '''', '\\'''), '''')"/>,
            'link': '<xsl:value-of select="@xlink:href"/>'}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
          <xsl:if test="position() != last()">,</xsl:if>
        </xsl:for-each-group>
        <xsl:if test="count($keywordWithNoThesaurus) > 0">
          <xsl:if test="count(//gmd:MD_Keywords[gmd:thesaurusName/*/gmd:title/*/text() != '']) > 0">,</xsl:if>
          'otherKeywords': [
          <xsl:for-each select="$keywordWithNoThesaurus/(gco:CharacterString|gmx:Anchor)">
            {'value': <xsl:value-of select="concat('''', replace(., '''', '\\'''), '''')"/>,
            'link': '<xsl:value-of select="@xlink:href"/>'}
            <xsl:if test="position() != last()">,</xsl:if>
          </xsl:for-each>
          ]
        </xsl:if>
        }
      </xsl:variable>

      <Field name="keywordGroup"
             string="{normalize-space($listOfKeywords)}"
             store="true"
             index="false"/>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:pointOfContact">
        <xsl:apply-templates mode="index-contact"
                             select="gmd:CI_ResponsibleParty|*[@gco:isoType = 'gmd:CI_ResponsibleParty']">
          <xsl:with-param name="type" select="'resource'"/>
          <xsl:with-param name="fieldPrefix" select="'responsibleParty'"/>
          <xsl:with-param name="position" select="position()"/>
        </xsl:apply-templates>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:choose>
        <xsl:when test="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
          <Field name="secConstr" string="true" store="true" index="true"/>
        </xsl:when>
        <xsl:otherwise>
          <Field name="secConstr" string="false" store="true" index="true"/>
        </xsl:otherwise>
      </xsl:choose>


      <!-- Add an extra value to the status codelist to indicate all
      non obsolete records -->
      <xsl:if test="$flagNonObseleteRecords">
        <xsl:variable name="isNotObsolete"
                      select="count(gmd:status[gmd:MD_ProgressCode/@codeListValue = 'obsolete']) = 0"/>
        <xsl:if test="$isNotObsolete">
          <Field name="cl_status" string="notobsolete" store="true" index="true"/>
        </xsl:if>
      </xsl:if>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:topicCategory/gmd:MD_TopicCategoryCode">
        <Field name="topicCat" string="{string(.)}" store="true" index="true"/>
        <Field name="keyword"
               string="{util:getCodelistTranslation('gmd:MD_TopicCategoryCode', string(.), string($isoLangId))}"
               store="true" index="true"/>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each
        select="gmd:language/gco:CharacterString|gmd:language/gmd:LanguageCode/@codeListValue">
        <Field name="datasetLang" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:spatialResolution/gmd:MD_Resolution">
        <xsl:for-each
          select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer">
          <Field name="denominator" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:distance/gco:Distance">
          <Field name="distanceVal" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:distance/gco:Distance/@uom">
          <Field name="distanceUom" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:distance/gco:Distance">
          <!-- Units may be encoded as
          http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/uom/ML_gmxUom.xml#m
          in such case retrieve the unit acronym only. -->
          <xsl:variable name="unit"
                        select="if (contains(@uom, '#')) then substring-after(@uom, '#') else @uom"/>
          <Field name="resolution" string="{concat(string(.), ' ', $unit)}" store="true"
                 index="true"/>
        </xsl:for-each>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

      <xsl:for-each select="gmd:resourceMaintenance/
                                gmd:MD_MaintenanceInformation/gmd:maintenanceAndUpdateFrequency/
                                gmd:MD_MaintenanceFrequencyCode/@codeListValue[. != '']">
        <Field name="updateFrequency" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>


      <xsl:for-each select="gmd:resourceConstraints/*">
        <xsl:variable name="fieldPrefix" select="local-name()"/>

        <xsl:for-each
          select="gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue[string(.) != 'otherRestrictions']">
          <Field name="{$fieldPrefix}AccessConstraints"
                 string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:otherConstraints/gco:CharacterString">
          <Field name="{$fieldPrefix}OtherConstraints"
                 string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:otherConstraints/gmx:Anchor[not(string(@xlink:href))]">
          <Field name="{$fieldPrefix}OtherConstraints"
                 string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:otherConstraints/gmx:Anchor[string(@xlink:href)]">
          <Field name="{$fieldPrefix}OtherConstraints"
                 string="{concat('link|',string(@xlink:href), '|', string(.))}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:useLimitation/gco:CharacterString">
            <Field name="{$fieldPrefix}UseLimitation"
                   string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:useLimitation/gmx:Anchor[not(string(@xlink:href))]">
            <Field name="{$fieldPrefix}UseLimitation"
                   string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:useLimitation/gmx:Anchor[string(@xlink:href)]">
            <Field name="{$fieldPrefix}UseLimitation"
                   string="{concat('link|',string(@xlink:href), '|', string(.))}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:useLimitation/gmx:Anchor[not(string(@xlink:href))]">
          <Field name="{$fieldPrefix}UseLimitation"
                 string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="gmd:useLimitation/gmx:Anchor[string(@xlink:href)]">
          <Field name="{$fieldPrefix}UseLimitation"
                 string="{concat('link|',string(@xlink:href), '|', string(.))}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>

      <!-- Index aggregation info and provides option to query by type of association
              and type of initiative

      Aggregation info is indexed by adding the following fields to the index:
       * agg_use: boolean
       * agg_with_association: {$associationType}
       * agg_{$associationType}: {$code}
       * agg_{$associationType}_with_initiative: {$initiativeType}
       * agg_{$associationType}_{$initiativeType}: {$code}

          Sample queries:
           * Search for records with siblings: http://localhost:8080/geonetwork/srv/fre/q?agg_use=true
           * Search for records having a crossReference with another record:
           http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
           * Search for records having a crossReference with another record:
           http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference=23f0478a-14ba-4a24-b365-8be88d5e9e8c
           * Search for records having a crossReference of type "study" with another record:
           http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_study=23f0478a-14ba-4a24-b365-8be88d5e9e8c
           * Search for records having a crossReference of type "study":
           http://localhost:8080/geonetwork/srv/fre/q?agg_crossReference_with_initiative=study
           * Search for records having a "crossReference" :
           http://localhost:8080/geonetwork/srv/fre/q?agg_with_association=crossReference
      -->
      <xsl:for-each select="gmd:aggregationInfo/gmd:MD_AggregateInformation">
        <xsl:variable name="code" select="gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString|
                                                  gmd:aggregateDataSetIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
        <xsl:if test="$code != ''">
          <xsl:variable name="associationType"
                        select="gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue"/>
          <xsl:variable name="initiativeType"
                        select="gmd:initiativeType/gmd:DS_InitiativeTypeCode/@codeListValue"/>

          <Field name="agg_{$associationType}_{$initiativeType}" string="{$code}" store="false"
                 index="true"/>
          <Field name="agg_{$associationType}_with_initiative" string="{$initiativeType}"
                 store="false" index="true"/>
          <Field name="agg_{$associationType}" string="{$code}" store="true" index="true"/>
          <Field name="agg_associated" string="{$code}" store="false" index="true"/>
          <Field name="agg_with_association" string="{$associationType}" store="false"
                 index="true"/>
          <Field name="agg_use" string="true" store="false" index="true"/>
        </xsl:if>
      </xsl:for-each>

      <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
      <!--  Fields use to search on Service -->

      <xsl:for-each select="srv:serviceType/gco:LocalName">
        <Field name="serviceType" string="{string(.)}" store="true" index="true"/>
        <Field  name="type" string="service-{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:serviceTypeVersion/gco:CharacterString">
        <Field name="serviceTypeVersion" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="//srv:SV_OperationMetadata/srv:operationName/gco:CharacterString">
        <Field name="operation" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:operatesOn/@uuidref">
        <Field name="operatesOn" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="srv:operatesOn/@xlink:href">
        <Field name="operatesOn" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>


      <xsl:for-each select="srv:coupledResource">
        <xsl:for-each select="srv:SV_CoupledResource/srv:identifier/gco:CharacterString">
          <Field name="operatesOnIdentifier" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>

        <xsl:for-each select="srv:SV_CoupledResource/srv:operationName/gco:CharacterString">
          <Field name="operatesOnName" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:for-each>

      <xsl:for-each
        select="gmd:graphicOverview/gmd:MD_BrowseGraphic[normalize-space(gmd:fileName/gco:CharacterString) != '']">
        <xsl:variable name="fileName" select="gmd:fileName/gco:CharacterString"/>
        <xsl:variable name="fileDescr" select="gmd:fileDescription/gco:CharacterString"/>
        <xsl:variable name="thumbnailType"
                      select="if (position() = 1) then 'thumbnail' else 'overview'"/>
        <!-- First thumbnail is flagged as thumbnail and could be considered the main one -->
        <Field name="image"
               string="{concat($thumbnailType, '|', $fileName, '|', $fileDescr)}"
               store="true" index="false"/>
      </xsl:for-each>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Distribution === -->

    <xsl:for-each select="gmd:distributionInfo/gmd:MD_Distribution">
      <xsl:for-each select="gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString">
        <Field name="format" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <!-- For local atom feed services -->
      <xsl:if test="count(gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download'])>0">
        <Field name="has_atom" string="true" store="false" index="true"/>
      </xsl:if>

      <!-- index online protocol -->
      <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions">
        <xsl:variable name="tPosition" select="position()"></xsl:variable>
        <xsl:for-each select="gmd:onLine/gmd:CI_OnlineResource[gmd:linkage/gmd:URL!='']">
          <xsl:variable name="download_check">
            <xsl:text>&amp;fname=&amp;access</xsl:text>
          </xsl:variable>
          <xsl:variable name="linkage" select="gmd:linkage/gmd:URL"/>
          <xsl:variable name="title"
                        select="normalize-space(gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType)"/>
          <xsl:variable name="desc" select="normalize-space(gmd:description/gco:CharacterString)"/>
          <xsl:variable name="protocol" select="normalize-space(gmd:protocol/gco:CharacterString)"/>
          <xsl:variable name="mimetype"
                        select="geonet:protocolMimeType($linkage, $protocol, gmd:name/gmx:MimeFileType/@type)"/>

          <!-- If the linkage points to WMS service and no protocol specified, manage as protocol OGC:WMS -->
          <xsl:variable name="wmsLinkNoProtocol"
                        select="contains(lower-case($linkage), 'service=wms') and not(string($protocol))"/>

          <!-- ignore empty downloads -->
          <xsl:if test="string($linkage)!='' and not(contains($linkage,$download_check))">
            <Field name="protocol" string="{string($protocol)}" store="true" index="true"/>
          </xsl:if>

          <xsl:if
            test="string($title)!='' and string($desc)!='' and not(contains($linkage,$download_check))">
            <Field name="linkage_name_des" string="{string(concat($title, ':::', $desc))}"
                   store="true" index="true"/>
          </xsl:if>

          <xsl:if test="normalize-space($mimetype)!=''">
            <Field name="mimetype" string="{$mimetype}" store="true" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'WWW:DOWNLOAD')">
            <Field name="download" string="true" store="false" index="true"/>
            <Field name="_mdActions" string="mdActions-download" store="false" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'OGC:WMS') or $wmsLinkNoProtocol">
            <Field name="dynamic" string="true" store="false" index="true"/>
            <Field name="_mdActions" string="mdActions-view" store="false" index="true"/>
          </xsl:if>

          <xsl:if test="contains($protocol, 'OGC:WPS')">
            <Field name="_mdActions" string="mdActions-process" store="false" index="true"/>
          </xsl:if>

          <!-- ignore WMS links without protocol (are indexed below with mimetype application/vnd.ogc.wms_xml) -->
          <xsl:if test="not($wmsLinkNoProtocol)">
            <Field name="link"
                   string="{concat($title, '|', $desc, '|', $linkage, '|', $protocol, '|', $mimetype, '|', $tPosition)}"
                   store="true" index="false"/>
          </xsl:if>

          <!-- Add KML link if WMS -->
          <xsl:if
            test="starts-with($protocol,'OGC:WMS') and string($linkage)!='' and string($title)!=''">

            <xsl:if test="$indexWmsAsKml">
              <!-- FIXME : relative path -->
              <Field name="link" string="{concat($title, '|', $desc, '|',
                                                  '../../srv/en/google.kml?uuid=', /gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString, '&amp;layers=', $title,
                                                  '|application/vnd.google-earth.kml+xml|application/vnd.google-earth.kml+xml', '|', $tPosition)}"
                     store="true" index="false"/>
            </xsl:if>
          </xsl:if>

          <!-- Try to detect Web Map Context by checking protocol or file extension -->
          <xsl:if test="starts-with($protocol,'OGC:WMC') or contains($linkage,'.wmc')">
            <Field name="link" string="{concat($title, '|', $desc, '|',
                                                $linkage, '|application/vnd.ogc.wmc|application/vnd.ogc.wmc', '|', $tPosition)}"
                   store="true" index="false"/>
          </xsl:if>
          <!-- Try to detect OWS Context by checking protocol or file extension -->
          <xsl:if test="starts-with($protocol,'OGC:OWS-C') or contains($linkage,'.ows')">
            <Field name="link" string="{concat($title, '|', $desc, '|',
                                                $linkage, '|application/vnd.ogc.ows|application/vnd.ogc.ows', '|', $tPosition)}"
                   store="true" index="false"/>
          </xsl:if>

          <xsl:if test="$wmsLinkNoProtocol">
            <Field name="link" string="{concat($title, '|', $desc, '|',
                                                $linkage, '|OGC:WMS|application/vnd.ogc.wms_xml', '|', $tPosition)}"
                   store="true" index="false"/>
          </xsl:if>
        </xsl:for-each>
      </xsl:for-each>

    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Content info === -->
    <xsl:for-each select="gmd:contentInfo/*/gmd:featureCatalogueCitation[@uuidref]">
      <Field name="hasfeaturecat" string="{string(@uuidref)}" store="false" index="true"/>
    </xsl:for-each>

    <!-- === Data Quality  === -->
    <xsl:for-each select="gmd:dataQualityInfo/*/gmd:lineage//gmd:source[@uuidref]">
      <Field name="hassource" string="{string(@uuidref)}" store="false" index="true"/>
    </xsl:for-each>

    <xsl:for-each select="gmd:dataQualityInfo/*/gmd:report/*/gmd:result">
      <xsl:if test="$inspire='true'">
        <!--
            INSPIRE related dataset could contains a conformity section with:
            * COMMISSION REGULATION (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services
            * INSPIRE Data Specification on <Theme Name> - <version>
            * INSPIRE Specification on <Theme Name> - <version> for CRS and GRID

            Index those types of citation title to found dataset related to INSPIRE (which may be better than keyword
            which are often used for other types of datasets).

            "1089/2010" is maybe too fuzzy but could work for translated citation like "Règlement n°1089/2010, Annexe II-6" TODO improved
        -->
        <xsl:if test="(
                                contains(gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString, '1089/2010') or
                                contains(gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString, 'INSPIRE Data Specification') or
                                contains(gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString, 'INSPIRE Specification'))">
          <Field name="inspirerelated" string="on" store="false" index="true"/>
        </xsl:if>
      </xsl:if>

      <xsl:for-each select="//gmd:pass/gco:Boolean">
        <Field name="degree" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="//gmd:specification/*/gmd:title/gco:CharacterString">
        <Field name="specificationTitle" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each select="//gmd:specification/*/gmd:date/*/gmd:date">
        <Field name="specificationDate" string="{string(gco:Date[.!='']|gco:DateTime[.!=''])}"
               store="true" index="true"/>
      </xsl:for-each>

      <xsl:for-each
        select="//gmd:specification/*/gmd:date/*/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue">
        <Field name="specificationDateType" string="{string(.)}" store="true" index="true"/>
      </xsl:for-each>
    </xsl:for-each>

    <xsl:for-each select="gmd:dataQualityInfo/*/gmd:lineage/*/gmd:statement/gco:CharacterString">
      <Field name="lineage" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === General stuff === -->
    <!-- Metadata type  -->

    <!-- Metadata on maps -->
    <xsl:variable name="isDataset"
                  select="
                  count(gmd:hierarchyLevel[gmd:MD_ScopeCode/@codeListValue='dataset']) > 0 or
                  count(gmd:hierarchyLevel) = 0"/>

    <xsl:variable name="isMapDigital" select="count(gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/
                        gmd:presentationForm[gmd:CI_PresentationFormCode/@codeListValue = 'mapDigital']) > 0"/>
    <xsl:variable name="isStatic" select="count(gmd:distributionInfo/gmd:MD_Distribution/
                        gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString[contains(., 'PDF') or contains(., 'PNG') or contains(., 'JPEG')]) > 0"/>
    <xsl:variable name="isInteractive" select="count(gmd:distributionInfo/gmd:MD_Distribution/
                        gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString[contains(., 'OGC:WMC') or contains(., 'OGC:OWS-C')]) > 0"/>
    <xsl:variable name="isPublishedWithWMCProtocol" select="count(gmd:distributionInfo/gmd:MD_Distribution/
                        gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:protocol[starts-with(gco:CharacterString, 'OGC:WMC')]) > 0"/>

    <xsl:choose>
      <xsl:when test="$isDataset and $isMapDigital and
                            ($isStatic or $isInteractive or $isPublishedWithWMCProtocol)">
        <Field name="type" string="map" store="true" index="true"/>
        <xsl:choose>
          <xsl:when test="$isStatic">
            <Field name="type" string="staticMap" store="true" index="true"/>
          </xsl:when>
          <xsl:when test="$isInteractive or $isPublishedWithWMCProtocol">
            <Field name="type" string="interactiveMap" store="true" index="true"/>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$isDataset">
        <Field name="type" string="dataset" store="true" index="true"/>
      </xsl:when>
      <xsl:when test="gmd:hierarchyLevel">
        <xsl:for-each select="gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue[.!='']">
          <Field name="type" string="{string(.)}" store="true" index="true"/>
        </xsl:for-each>
      </xsl:when>
    </xsl:choose>


    <xsl:choose>
      <!-- Check if metadata is a service metadata record -->
      <xsl:when test="gmd:identificationInfo/srv:SV_ServiceIdentification">
        <Field name="type" string="service" store="false" index="true"/>
      </xsl:when>
      <!-- <xsl:otherwise>
      ... gmd:*_DataIdentification / hierachicalLevel is used and return dataset, serie, ...
      </xsl:otherwise>-->
    </xsl:choose>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:hierarchyLevelName/gco:CharacterString">
      <Field name="levelName" string="{string(.)}" store="false" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:language/gco:CharacterString
                        |gmd:language/gmd:LanguageCode/@codeListValue
                        |gmd:locale/gmd:PT_Locale/gmd:languageCode/gmd:LanguageCode/@codeListValue">
      <Field name="language" string="{string(.)}" store="true" index="true"/>
      <Field name="mdLanguage" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:fileIdentifier/gco:CharacterString">
      <Field name="fileId" string="{string(.)}" store="false" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:parentIdentifier/gco:CharacterString">
      <Field name="parentUuid" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>
    <Field name="isChild" string="{exists(gmd:parentIdentifier)}" store="true" index="true"/>


    <xsl:for-each select="gmd:metadataStandardName/gco:CharacterString">
      <Field name="standardName" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->

    <xsl:for-each select="gmd:dateStamp/*">
      <Field name="changeDate" string="{string(.)}" store="true" index="true"/>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <xsl:for-each select="gmd:contact">
      <Field name="metadataPOC"
             string="{string(*/gmd:organisationName/(gco:CharacterString|gmx:Anchor))}"
             store="true" index="true"/>
      <xsl:apply-templates mode="index-contact" select="*">
        <xsl:with-param name="type" select="'metadata'"/>
        <xsl:with-param name="fieldPrefix" select="'responsibleParty'"/>
        <xsl:with-param name="position" select="position()"/>
      </xsl:apply-templates>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Reference system info === -->

    <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
        <xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
            <xsl:variable name="crs">
                <xsl:for-each select="gmd:codeSpace/*/text() | gmd:code/*/text()">
                    <xsl:value-of select="."/>
                    <xsl:if test="not(position() = last())">::</xsl:if>
                </xsl:for-each>
            </xsl:variable>

            <xsl:if test="$crs != ''">
                <Field name="crs" string="{$crs}" store="true" index="true"/>
            </xsl:if>

            <xsl:variable name="crsDetails">
            {
              "code": "<xsl:value-of select="gmd:code/*/text()"/>",
              "codeSpace": "<xsl:value-of select="gmd:codeSpace/*/text()"/>",
              "name": "<xsl:value-of select="gmd:code/*/@xlink:title"/>",
              "url": "<xsl:value-of select="gmd:code/*/@xlink:href"/>"
            }
            </xsl:variable>

            <Field name="crsDetails"
                   string="{normalize-space($crsDetails)}"
                   store="true"
                   index="false"/>
        </xsl:for-each>
    </xsl:for-each>

    <xsl:for-each select="gmd:referenceSystemInfo/gmd:MD_ReferenceSystem">
      <xsl:for-each select="gmd:referenceSystemIdentifier/gmd:RS_Identifier">
        <Field name="authority" string="{string(gmd:codeSpace/gco:CharacterString)}" store="false"
               index="true"/>
        <Field name="crsCode" string="{string(gmd:code/gco:CharacterString)}" store="false"
               index="true"/>
        <Field name="crsVersion" string="{string(gmd:version/gco:CharacterString)}" store="false"
               index="true"/>
      </xsl:for-each>
    </xsl:for-each>

    <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <!-- === Free text search === -->

    <Field name="any" store="false" index="true">
      <xsl:attribute name="string">
        <xsl:value-of select="normalize-space(string(.))"/>
        <xsl:text> </xsl:text>
        <xsl:for-each select="//@codeListValue">
          <xsl:value-of select="concat(., ' ')"/>
        </xsl:for-each>
      </xsl:attribute>
    </Field>

    <xsl:variable name="identification" select="gmd:identificationInfo//gmd:MD_DataIdentification|
                        gmd:identificationInfo//*[contains(@gco:isoType, 'MD_DataIdentification')]|
                        gmd:identificationInfo/srv:SV_ServiceIdentification"/>


    <Field name="anylight" store="false" index="true">
      <xsl:attribute name="string">
        <xsl:for-each
          select="$identification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString|
                    $identification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle/gco:CharacterString|
                    $identification/gmd:abstract/gco:CharacterString|
                    $identification/gmd:credit/gco:CharacterString|
                    $identification//gmd:organisationName/gco:CharacterString|
                    $identification/gmd:supplementalInformation/gco:CharacterString|
                    $identification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString|
                    $identification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gmx:Anchor">
          <xsl:value-of select="concat(., ' ')"/>
        </xsl:for-each>
      </xsl:attribute>
    </Field>


    <!-- Index all codelist -->
    <xsl:for-each select=".//*[*/@codeListValue != '']">
      <Field name="cl_{local-name()}"
             string="{*/@codeListValue}"
             store="true" index="true"/>
      <Field name="cl_{concat(local-name(), '_text')}"
             string="{util:getCodelistTranslation(name(*), string(*/@codeListValue), string($isoLangId))}"
             store="true" index="true"/>
    </xsl:for-each>
  </xsl:template>


  <xsl:template mode="index-contact" match="gmd:CI_ResponsibleParty|*[@gco:isoType = 'gmd:CI_ResponsibleParty']">
    <xsl:param name="type"/>
    <xsl:param name="fieldPrefix"/>
    <xsl:param name="position" select="'0'"/>

    <xsl:variable name="orgName" select="gmd:organisationName/(gco:CharacterString|gmx:Anchor)"/>

    <Field name="orgName" string="{string($orgName)}" store="true" index="true"/>
    <Field name="orgNameTree" string="{string($orgName)}" store="true" index="true"/>

    <xsl:variable name="uuid" select="@uuid"/>
    <xsl:variable name="role" select="gmd:role/*/@codeListValue"/>
    <xsl:variable name="roleTranslation"
                  select="util:getCodelistTranslation('gmd:CI_RoleCode', string($role), string($isoLangId))"/>
    <xsl:variable name="logo" select=".//gmx:FileName/@src"/>
    <xsl:variable name="website" select=".//gmd:onlineResource/*/gmd:linkage/gmd:URL"/>
    <xsl:variable name="email"
                  select="gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/gco:CharacterString"/>
    <xsl:variable name="phone"
                  select="gmd:contactInfo/*/gmd:phone/*/gmd:voice[normalize-space(.) != '']/*/text()"/>
    <xsl:variable name="individualName"
                  select="gmd:individualName/gco:CharacterString/text()"/>
    <xsl:variable name="positionName"
                  select="gmd:positionName/gco:CharacterString/text()"/>
    <xsl:variable name="address" select="string-join(gmd:contactInfo/*/gmd:address/*/(
                                        gmd:deliveryPoint|gmd:postalCode|gmd:city|
                                        gmd:administrativeArea|gmd:country)/gco:CharacterString/text(), ', ')"/>

    <Field name="{$fieldPrefix}"
           string="{concat($roleTranslation, '|',
                             $type,'|',
                             $orgName, '|',
                             $logo, '|',
                             string-join($email, ','), '|',
                             $individualName, '|',
                             $positionName, '|',
                             $address, '|',
                             string-join($phone, ','), '|',
                             $uuid, '|',
                             $position, '|',
                             $website)}"
           store="true" index="false"/>

    <xsl:for-each select="$email">
      <Field name="{$fieldPrefix}Email" string="{string(.)}" store="true" index="true"/>
      <Field name="{$fieldPrefix}RoleAndEmail" string="{$role}|{string(.)}" store="true" index="true"/>
    </xsl:for-each>
    <xsl:for-each select="@uuid">
      <Field name="{$fieldPrefix}Uuid" string="{string(.)}" store="true" index="true"/>
      <Field name="{$fieldPrefix}RoleAndUuid" string="{$role}|{string(.)}" store="true" index="true"/>
    </xsl:for-each>
  </xsl:template>

  <!-- ========================================================================================= -->

</xsl:stylesheet>
