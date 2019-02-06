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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml"
                xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gn-fn-iso19139="http://geonetwork-opensource.org/xsl/functions/profiles/iso19139"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                version="2.0" exclude-result-prefixes="#all">

  <xsl:include href="../iso19139/convert/functions.xsl"/>
  <xsl:include href="update-fixed-info-keywords.xsl"/>
  <xsl:include href="layout/utility-fn.xsl"/>


  <xsl:variable name="serviceUrl" select="/root/env/siteURL"/>
  <xsl:variable name="node" select="/root/env/node"/>

  <!-- We use the category check to find out if this is an SDS metadata. Please replace with anything better -->
  <xsl:variable name="isSDS"
                select="count(//gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult/gmd:specification/gmd:CI_Citation/gmd:title/gmx:Anchor[starts-with(@xlink:href, 'http://inspire.ec.europa.eu/metadata-codelist/Category')]) = 1"/>


  <!-- The default language is also added as gmd:locale
  for multilingual metadata records. -->
  <xsl:variable name="mainLanguage"
                select="/root/*/gmd:language/gco:CharacterString/text()|
                        /root/*/gmd:language/gmd:LanguageCode/@codeListValue"/>

  <xsl:variable name="isMultilingual"
                select="count(/root/*/gmd:locale[*/gmd:languageCode/*/@codeListValue != $mainLanguage]) > 0"/>

  <xsl:variable name="mainLanguageId"
                select="upper-case(java:twoCharLangCode($mainLanguage))"/>

  <xsl:variable name="locales"
                select="/root/*/gmd:locale/gmd:PT_Locale"/>

  <xsl:variable name="defaultEncoding"
                select="'utf8'"/>

  <xsl:variable name="editorConfig"
                select="document('layout/config-editor.xml')"/>

  <xsl:variable name="nonMultilingualFields"
                select="$editorConfig/editor/multilingualFields/exclude"/>



  <xsl:template match="/root">
    <xsl:apply-templates select="*:MD_Metadata"/>
  </xsl:template>



  <xsl:template match="gmd:MD_Metadata">
    <xsl:copy>
      <xsl:namespace name="xsi" select="'http://www.w3.org/2001/XMLSchema-instance'"/>
      <xsl:apply-templates select="@*"/>

      <gmd:fileIdentifier>
        <gco:CharacterString>
          <xsl:value-of select="/root/env/uuid"/>
        </gco:CharacterString>
      </gmd:fileIdentifier>

      <xsl:apply-templates select="gmd:language"/>
      <xsl:apply-templates select="gmd:characterSet"/>

      <xsl:choose>
        <xsl:when test="/root/env/parentUuid!=''">
          <gmd:parentIdentifier>
            <gco:CharacterString>
              <xsl:value-of select="/root/env/parentUuid"/>
            </gco:CharacterString>
          </gmd:parentIdentifier>
        </xsl:when>
        <xsl:when test="gmd:parentIdentifier">
          <xsl:apply-templates select="gmd:parentIdentifier"/>
        </xsl:when>
      </xsl:choose>

      <xsl:apply-templates select="
        gmd:hierarchyLevel|
        gmd:hierarchyLevelName|
        gmd:contact|
        gmd:dateStamp|
        gmd:metadataStandardName|
        gmd:metadataStandardVersion|
        gmd:dataSetURI"/>

      <!-- Copy existing locales and create an extra one for the default metadata language. -->
      <xsl:if test="$isMultilingual">
        <xsl:apply-templates select="gmd:locale[*/gmd:languageCode/*/@codeListValue != $mainLanguage]"/>
        <gmd:locale>
          <gmd:PT_Locale id="{$mainLanguageId}">
            <gmd:languageCode>
              <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                                codeListValue="{$mainLanguage}"/>
            </gmd:languageCode>
            <gmd:characterEncoding>
              <gmd:MD_CharacterSetCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_CharacterSetCode"
                                       codeListValue="{$defaultEncoding}"/>
            </gmd:characterEncoding>
          </gmd:PT_Locale>
        </gmd:locale>
      </xsl:if>

      <xsl:apply-templates select="
        gmd:spatialRepresentationInfo|
        gmd:referenceSystemInfo|
        gmd:metadataExtensionInfo|
        gmd:identificationInfo|
        gmd:contentInfo|
        gmd:distributionInfo|
        gmd:dataQualityInfo|
        gmd:portrayalCatalogueInfo|
        gmd:metadataConstraints|
        gmd:applicationSchemaInfo|
        gmd:metadataMaintenance|
        gmd:series|
        gmd:describes|
        gmd:propertyType|
        gmd:featureType|
        gmd:featureAttribute"/>

      <!-- Handle ISO profiles extensions. -->
      <xsl:apply-templates select="
        *[namespace-uri()!='http://www.isotc211.org/2005/gmd' and
          namespace-uri()!='http://www.isotc211.org/2005/srv']"/>
    </xsl:copy>
  </xsl:template>



  <!-- ================================================================= -->

  <xsl:template match="gmd:dateStamp">
    <xsl:choose>
      <xsl:when test="/root/env/changeDate">
        <xsl:copy>
          <gco:DateTime>
            <xsl:value-of select="/root/env/changeDate"/>
          </gco:DateTime>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ================================================================= -->

  <!-- Only set metadataStandardName and metadataStandardVersion
    if not set. -->
  <xsl:template match="gmd:metadataStandardName[@gco:nilReason='missing' or gco:CharacterString='']"
                priority="10">
    <xsl:copy>
      <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <xsl:template
    match="gmd:metadataStandardVersion[@gco:nilReason='missing' or gco:CharacterString='']"
    priority="10">
    <xsl:copy>
      <gco:CharacterString>1.0</gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template
    match="gmd:topicCategory[not(gmd:MD_TopicCategoryCode)]"
    priority="10" />

  <!-- ================================================================= -->

  <xsl:template match="@gml:id">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="gml:id">
          <xsl:value-of select="generate-id(.)"/>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ==================================================================== -->
  <!-- Fix srsName attribute generate CRS:84 (EPSG:4326 with long/lat
         ordering) by default -->

  <xsl:template match="@srsName">
    <xsl:choose>
      <xsl:when test="normalize-space(.)=''">
        <xsl:attribute name="srsName">
          <xsl:text>CRS:84</xsl:text>
        </xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Add required gml attributes if missing -->
  <xsl:template match="gml:Polygon[not(@gml:id) and not(@srsName)]">
    <xsl:copy>
      <xsl:attribute name="gml:id">
        <xsl:value-of select="generate-id(.)"/>
      </xsl:attribute>
      <xsl:attribute name="srsName">
        <xsl:text>urn:x-ogc:def:crs:EPSG:6.6:4326</xsl:text>
      </xsl:attribute>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="*"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->

  <xsl:template match="*[gco:CharacterString|gmx:Anchor|gmd:PT_FreeText]">
    <xsl:copy>
      <xsl:apply-templates select="@*[not(name() = 'gco:nilReason') and not(name() = 'xsi:type')]"/>

      <!-- Add nileason if text is empty -->
      <xsl:variable name="excluded"
                    select="gn-fn-iso19139:isNotMultilingualField(., $editorConfig)"/>


      <xsl:variable name="valueInPtFreeTextForMainLanguage"
                    select="normalize-space(gmd:PT_FreeText/*/gmd:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)])"/>

      <!-- Add nileason if text is empty -->
      <xsl:variable name="isEmpty"
                    select="if ($isMultilingual and not($excluded))
                            then $valueInPtFreeTextForMainLanguage = ''
                            else if ($valueInPtFreeTextForMainLanguage != '')
                            then $valueInPtFreeTextForMainLanguage = ''
                            else normalize-space(gco:CharacterString|gmx:Anchor) = ''"/>

      <!-- TODO ? Removes @nilReason from parents of gmx:Anchor if anchor has @xlink:href attribute filled. -->
      <xsl:variable name="isEmptyAnchor"
                    select="normalize-space(gmx:Anchor/@xlink:href) = ''" />


      <xsl:choose>
        <xsl:when test="$isEmpty">
          <xsl:attribute name="gco:nilReason">
            <xsl:choose>
              <xsl:when test="@gco:nilReason">
                <xsl:value-of select="@gco:nilReason"/>
              </xsl:when>
              <xsl:otherwise>missing</xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="@gco:nilReason != 'missing' and not($isEmpty)">
          <xsl:copy-of select="@gco:nilReason"/>
        </xsl:when>
      </xsl:choose>


      <!-- For multilingual records, for multilingual fields,
       create a gco:CharacterString or gmx:Anchor containing
       the same value as the default language PT_FreeText.
      -->
      <xsl:variable name="element" select="name()"/>


      <xsl:choose>
        <!-- Check record does not contains multilingual elements
          matching the main language. This may happen if the main
          language is declared in locales and only PT_FreeText are set.
          It should not be possible in GeoNetwork, but record user can
          import may use this encoding. -->
        <xsl:when test="not($isMultilingual) and
                        $valueInPtFreeTextForMainLanguage != '' and
                        normalize-space(gco:CharacterString|gmx:Anchor) = ''">
          <xsl:element name="{if (gmx:Anchor) then 'gmx:Anchor' else 'gco:CharacterString'}">
            <xsl:copy-of select="gmx:Anchor/@*"/>
            <xsl:value-of select="$valueInPtFreeTextForMainLanguage"/>
          </xsl:element>
        </xsl:when>
        <xsl:when test="not($isMultilingual) or
                        $excluded">
          <!-- Copy gco:CharacterString only. PT_FreeText are removed if not multilingual. -->
          <xsl:apply-templates select="gco:CharacterString|gmx:Anchor"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- Add xsi:type for multilingual element. -->
          <xsl:attribute name="xsi:type" select="'gmd:PT_FreeText_PropertyType'"/>

          <!-- Is the default language value set in a PT_FreeText ? -->
          <xsl:variable name="isInPTFreeText"
                        select="count(gmd:PT_FreeText/*/gmd:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)]) = 1"/>


          <xsl:choose>
            <xsl:when test="$isInPTFreeText">
              <!-- Update gco:CharacterString to contains
                   the default language value from the PT_FreeText.
                   PT_FreeText takes priority. -->
              <xsl:element name="{if (gmx:Anchor) then 'gmx:Anchor' else 'gco:CharacterString'}">
                <xsl:copy-of select="gmx:Anchor/@*"/>
                <xsl:value-of select="gmd:PT_FreeText/*/gmd:LocalisedCharacterString[
                                            @locale = concat('#', $mainLanguageId)]/text()"/>
              </xsl:element>

              <xsl:if test="gmd:PT_FreeText[normalize-space(.) != '']">
                <gmd:PT_FreeText>
                  <xsl:call-template name="populate-free-text"/>
                </gmd:PT_FreeText>
              </xsl:if>

            </xsl:when>
            <xsl:otherwise>

              <!-- Populate PT_FreeText for default language if not existing. -->
              <xsl:apply-templates select="gco:CharacterString|gmx:Anchor"/>
              <gmd:PT_FreeText>
                <gmd:textGroup>
                  <gmd:LocalisedCharacterString locale="#{$mainLanguageId}">
                    <xsl:value-of select="gco:CharacterString"/>
                  </gmd:LocalisedCharacterString>
                </gmd:textGroup>
                <xsl:call-template name="populate-free-text"/>
              </gmd:PT_FreeText>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>


  <xsl:template name="populate-free-text">
    <xsl:variable name="freeText"
                  select="gmd:PT_FreeText/gmd:textGroup"/>

    <!-- Loop on locales in order to preserve order.
        Keep main language on top.
        Translations having no locale are ignored. eg. when removing a lang. -->
    <xsl:for-each select="$locales[@id = $mainLanguageId]">
      <xsl:variable name="localId"
                    select="@id"/>

      <xsl:variable name="element"
                    select="$freeText[*/@locale = concat('#', $localId)]"/>

      <xsl:apply-templates select="$element"/>
    </xsl:for-each>

    <xsl:for-each select="$locales[@id != $mainLanguageId]">
      <xsl:variable name="localId"
                    select="@id"/>

      <xsl:variable name="element"
                    select="$freeText[*/@locale = concat('#', $localId)]"/>

      <xsl:apply-templates select="$element"/>
    </xsl:for-each>
  </xsl:template>

  <!-- For multilingual elements. Check that the local
  is defined in record. If not, remove the element. -->
  <xsl:template match="gmd:textGroup">
    <xsl:variable name="elementLocalId"
                  select="replace(gmd:LocalisedCharacterString/@locale, '^#', '')"/>
    <xsl:choose>
      <xsl:when test="count($locales[@id = $elementLocalId]) > 0">
        <gmd:textGroup>
          <gmd:LocalisedCharacterString>
            <xsl:variable name="currentLocale"
                          select="replace(gmd:LocalisedCharacterString/@locale, '^#', '')"/>
            <xsl:variable name="ptLocale"
                          select="$locales[@id = string($currentLocale)]"/>
            <xsl:variable name="id"
                          select="upper-case(java:twoCharLangCode($ptLocale/gmd:languageCode/gmd:LanguageCode/@codeListValue[. != '']))"/>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="$id != ''">
              <xsl:attribute name="locale">
                <xsl:value-of select="concat('#',$id)"/>
              </xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="gmd:LocalisedCharacterString/text()"/>
          </gmd:LocalisedCharacterString>
        </gmd:textGroup>
      </xsl:when>
      <xsl:otherwise>
        <!--<xsl:message>Removing <xsl:copy-of select="."/>.
        This element was removed because not declared in record locales.</xsl:message>-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ================================================================= -->
  <!-- codelists: set @codeList path -->
  <!-- ================================================================= -->
  <xsl:template match="gmd:LanguageCode[@codeListValue]" priority="10">
    <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/">
      <xsl:apply-templates select="@*[name(.)!='codeList']"/>
    </gmd:LanguageCode>
  </xsl:template>


  <xsl:template match="gmd:*[@codeListValue]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of
          select="concat('http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#',local-name(.))"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>

  <!-- can't find the location of the 19119 codelists - so we make one up -->

  <xsl:template match="srv:*[@codeListValue]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of
          select="concat('http://www.isotc211.org/2005/iso19119/resources/Codelist/gmxCodelists.xml#',local-name(.))"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>
  <!-- ================================================================= -->
  <!-- online resources: download -->
  <!-- ================================================================= -->

  <xsl:template
    match="gmd:CI_OnlineResource[matches(gmd:protocol/gco:CharacterString,'^WWW:DOWNLOAD-.*-http--download.*') and gmd:name]">
    <xsl:variable name="fname" select="gmd:name/gco:CharacterString|gmd:name/gmx:MimeFileType"/>
    <xsl:variable name="mimeType">
      <xsl:call-template name="getMimeTypeFile">
        <xsl:with-param name="datadir" select="/root/env/datadir"/>
        <xsl:with-param name="fname" select="$fname"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <gmd:linkage>
        <gmd:URL>
          <xsl:value-of select="gmd:linkage/gmd:URL"/>
        </gmd:URL>
      </gmd:linkage>
      <xsl:copy-of select="gmd:protocol"/>
      <xsl:copy-of select="gmd:applicationProfile"/>
      <gmd:name>
        <gmx:MimeFileType type="{$mimeType}">
          <xsl:value-of select="$fname"/>
        </gmx:MimeFileType>
      </gmd:name>
      <xsl:copy-of select="gmd:description"/>
      <xsl:copy-of select="gmd:function"/>
    </xsl:copy>
  </xsl:template>

  <!-- ================================================================= -->
  <!-- Add mime type for downloadable online resources -->
  <!-- ================================================================= -->

  <xsl:template
    match="gmd:CI_OnlineResource[starts-with(gmd:protocol/gco:CharacterString,'WWW:LINK-') and contains(gmd:protocol/gco:CharacterString,'http--download')]">
    <xsl:variable name="mimeType">
      <xsl:call-template name="getMimeTypeUrl">
        <xsl:with-param name="linkage" select="gmd:linkage/gmd:URL"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="gmd:linkage"/>
      <xsl:copy-of select="gmd:protocol"/>
      <xsl:copy-of select="gmd:applicationProfile"/>
      <gmd:name>
        <gmx:MimeFileType type="{$mimeType}"/>
      </gmd:name>
      <xsl:copy-of select="gmd:description"/>
      <xsl:copy-of select="gmd:function"/>
    </xsl:copy>
  </xsl:template>


  <!-- ================================================================= -->

  <!-- Do not allow to expand operatesOn sub-elements
        and constrain users to use uuidref attribute to link
        service metadata to datasets. This will avoid to have
        error on XSD validation. -->

  <xsl:template match="srv:operatesOn|gmd:featureCatalogueCitation">
    <xsl:copy>
      <xsl:copy-of select="@uuidref"/>
      <xsl:choose>

        <!-- Do not expand operatesOn sub-elements when using uuidref
             to link service metadata to datasets or datasets to iso19110.
         -->
        <xsl:when test="@uuidref">
          <xsl:choose>
            <xsl:when test="not(string(@xlink:href)) or starts-with(@xlink:href, $serviceUrl)">
              <xsl:attribute name="xlink:href">
                <xsl:value-of
                  select="concat($serviceUrl,'csw?service=CSW&amp;request=GetRecordById&amp;version=2.0.2&amp;outputSchema=http://www.isotc211.org/2005/gmd&amp;elementSetName=full&amp;id=',@uuidref)"/>
              </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:copy-of select="@xlink:href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>

        <xsl:otherwise>
          <xsl:apply-templates select="node()" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>


  <!-- ================================================================= -->
  <!-- Set local identifier to the first 2 letters of iso code. Locale ids
        are used for multilingual charcterString using #iso2code for referencing.
    -->
  <xsl:template match="gmd:PT_Locale">
    <xsl:element name="gmd:{local-name()}">
      <xsl:variable name="id"
                    select="upper-case(java:twoCharLangCode(gmd:languageCode/gmd:LanguageCode/@codeListValue, ''))"/>

      <xsl:apply-templates select="@*"/>
      <xsl:if test="normalize-space(@id)='' or normalize-space(@id)!=$id">
        <xsl:attribute name="id">
          <xsl:value-of select="$id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>




  <!-- Remove attribute indeterminatePosition having empty
       value which is not a valid facet for it. -->
  <xsl:template match="@indeterminatePosition[. = '']" priority="2"/>




  <!-- ================================================================= -->
  <!-- Adjust the namespace declaration - In some cases name() is used to get the
        element. The assumption is that the name is in the format of  <ns:element>
        however in some cases it is in the format of <element xmlns=""> so the
        following will convert them back to the expected value. This also corrects the issue
        where the <element xmlns=""> loose the xmlns="" due to the exclude-result-prefixes="#all" -->
  <!-- Note: Only included prefix gml, gmd and gco for now. -->
  <!-- TODO: Figure out how to get the namespace prefix via a function so that we don't need to hard code them -->
  <!-- ================================================================= -->

  <xsl:template name="correct_ns_prefix">
    <xsl:param name="element"/>
    <xsl:param name="prefix"/>
    <xsl:choose>
      <xsl:when test="local-name($element)=name($element) and $prefix != '' ">
        <xsl:element name="{$prefix}:{local-name($element)}">
          <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="gmd:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gmd'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gco:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gco'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="gml:*">
    <xsl:call-template name="correct_ns_prefix">
      <xsl:with-param name="element" select="."/>
      <xsl:with-param name="prefix" select="'gml'"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ================================================================= -->
  <!-- SDS fixes -->

  <!-- DCP codelist -->
  <xsl:template match="srv:DCP[$isSDS]/srv:DCPList[@codeListValue]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="codeList">
        <xsl:value-of select="'http://inspire.ec.europa.eu/metadata-codelist/DCPList'"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>


  <!-- Remove geographic/temporal extent if doesn't contain child elements.
       Used to clean up the element, for example when removing the the temporal extent
       in the editor, to avoid an element like <gmd:extent><gmd:EX_Extent></gmd:EX_Extent></gmd:extent>,
       that causes a validation error in schematron iso: [ISOFTDS19139:2005-TableA1-Row23] - Extent element required
  -->
  <xsl:template match="gmd:extent[gmd:EX_Extent/not(*)]|srv:extent[gmd:EX_Extent/not(*)]"/>


  <!-- ================================================================= -->
  <!-- copy everything else as is -->

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@xsi:schemaLocation">
    <xsl:if test="java:getSettingValue('system/metadata/validation/removeSchemaLocation') = 'false'">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
