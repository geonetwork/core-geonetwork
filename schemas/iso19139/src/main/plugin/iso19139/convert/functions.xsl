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

<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:date="http://exslt.org/dates-and-times"
                xmlns:java="java:org.fao.geonet.util.XslUtil"
                xmlns:joda="java:org.fao.geonet.domain.ISODate"
                xmlns:mime="java:org.fao.geonet.util.MimeTypeFinder"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- ========================================================================================= -->
  <!-- latlon coordinates indexed as numeric. -->

  <xsl:template match="*" mode="latLon">
    <xsl:variable name="format" select="'##.00'"></xsl:variable>

    <xsl:if test="number(gmd:westBoundLongitude/gco:Decimal)
            and number(gmd:southBoundLatitude/gco:Decimal)
            and number(gmd:eastBoundLongitude/gco:Decimal)
            and number(gmd:northBoundLatitude/gco:Decimal)
            ">
      <Field name="westBL" string="{format-number(gmd:westBoundLongitude/gco:Decimal, $format)}"
             store="false" index="true"/>
      <Field name="southBL" string="{format-number(gmd:southBoundLatitude/gco:Decimal, $format)}"
             store="false" index="true"/>

      <Field name="eastBL" string="{format-number(gmd:eastBoundLongitude/gco:Decimal, $format)}"
             store="false" index="true"/>
      <Field name="northBL" string="{format-number(gmd:northBoundLatitude/gco:Decimal, $format)}"
             store="false" index="true"/>

      <Field name="geoBox" string="{concat(gmd:westBoundLongitude/gco:Decimal, '|',
                gmd:southBoundLatitude/gco:Decimal, '|',
                gmd:eastBoundLongitude/gco:Decimal, '|',
                gmd:northBoundLatitude/gco:Decimal
                )}" store="true" index="false"/>
    </xsl:if>

  </xsl:template>
  <!-- ================================================================== -->

  <xsl:template name="fixSingle">
    <xsl:param name="value"/>

    <xsl:choose>
      <xsl:when test="string-length(string($value))=1">
        <xsl:value-of select="concat('0',$value)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ================================================================== -->

  <xsl:template name="getMimeTypeFile">
    <xsl:param name="datadir"/>
    <xsl:param name="fname"/>
    <xsl:value-of select="mime:detectMimeTypeFile($datadir,$fname)"/>
  </xsl:template>

  <!-- ==================================================================== -->

  <xsl:template name="getMimeTypeUrl">
    <xsl:param name="linkage"/>
    <xsl:value-of select="mime:detectMimeTypeUrl($linkage)"/>
  </xsl:template>

  <!-- ==================================================================== -->
  <xsl:template name="fixNonIso">
    <xsl:param name="value"/>

    <xsl:variable name="now" select="date:date-time()"/>
    <xsl:choose>
      <xsl:when
        test="$value='' or lower-case($value)='unknown' or lower-case($value)='current' or lower-case($value)='now'">
        <xsl:variable name="miy" select="date:month-in-year($now)"/>
        <xsl:variable name="month">
          <xsl:call-template name="fixSingle">
            <xsl:with-param name="value" select="$miy"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="dim" select="date:day-in-month($now)"/>
        <xsl:variable name="day">
          <xsl:call-template name="fixSingle">
            <xsl:with-param name="value" select="$dim"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat(date:year($now),'-',$month,'-',$day,'T23:59:59')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ==================================================================== -->

  <xsl:template name="newGmlTime">
    <xsl:param name="begin"/>
    <xsl:param name="end"/>


    <xsl:variable name="value1">
      <xsl:call-template name="fixNonIso">
        <xsl:with-param name="value" select="normalize-space($begin)"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="value2">
      <xsl:call-template name="fixNonIso">
        <xsl:with-param name="value" select="normalize-space($end)"/>
      </xsl:call-template>
    </xsl:variable>

    <!-- must be a full ISODateTimeFormat - so parse it and make sure it is
             returned as a long format using the joda Java Time library -->
    <xsl:variable name="output" select="joda:parseISODateTimes($value1,$value2)"/>
    <xsl:value-of select="$output"/>

  </xsl:template>

  <!-- ================================================================== -->
  <!-- iso3code of default index language -->
  <xsl:variable name="defaultLang">eng</xsl:variable>

  <xsl:template name="langId19139">
    <xsl:variable name="tmp">
      <xsl:choose>
        <xsl:when test="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gco:CharacterString|
                                /*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gmd:LanguageCode/@codeListValue">
          <xsl:value-of select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gco:CharacterString|
                                /*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:language/gmd:LanguageCode/@codeListValue"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$defaultLang"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="normalize-space(string($tmp))"></xsl:value-of>
  </xsl:template>


  <xsl:template name="defaultTitle">
    <xsl:param name="isoDocLangId"/>

    <xsl:variable name="poundLangId"
                  select="concat('#',upper-case(java:twoCharLangCode($isoDocLangId)))"/>

    <xsl:variable name="identification" select="/*[name(.)='gmd:MD_Metadata' or
            name() = 'gmi:MI_Metadata' or
            @gco:isoType='gmd:MD_Metadata' or
            @gco:isoType='gmd:MI_Metadata']/gmd:identificationInfo/*"></xsl:variable>
    <xsl:variable name="docLangTitle"
                  select="$identification/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[@locale=$poundLangId]"/>
    <xsl:variable name="charStringTitle"
                  select="$identification/gmd:citation/*/gmd:title/gco:CharacterString"/>
    <xsl:variable name="locStringTitles"
                  select="$identification/gmd:citation/*/gmd:title//gmd:LocalisedCharacterString[. != '']"/>
    <xsl:choose>
      <xsl:when test="string-length(string($docLangTitle)) != 0">
        <xsl:value-of select="$docLangTitle[1]"/>
      </xsl:when>
      <xsl:when test="string-length(string($charStringTitle[1])) != 0">
        <xsl:value-of select="string($charStringTitle[1])"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string($locStringTitles[1])"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <xsl:template name="defaultAbstract">
    <xsl:param name="isoDocLangId"/>

    <xsl:variable name="poundLangId"
                  select="concat('#',upper-case(java:twoCharLangCode($isoDocLangId)))" />

    <xsl:variable name="identification"
                  select="/*[name(.)='gmd:MD_Metadata' or @gco:isoType='gmd:MD_Metadata']/gmd:identificationInfo/*[name(.)='gmd:MD_DataIdentification' or @gco:isoType='gmd:MD_DataIdentification' or name(.)='srv:SV_ServiceIdentification' or @gco:isoType='srv:SV_ServiceIdentification']"></xsl:variable>
    <xsl:variable name="docLangAbstract"
                  select="$identification/gmd:abstract//gmd:LocalisedCharacterString[@locale=$poundLangId]"/>
    <xsl:variable name="charStringAbstract"
                  select="$identification/gmd:abstract/gco:CharacterString"/>
    <xsl:variable name="locStringAbstracts"
                  select="$identification/gmd:abstract//gmd:LocalisedCharacterString[. != '']"/>
    <xsl:choose>
      <xsl:when test="string-length(string($docLangAbstract[1])) != 0">
        <xsl:value-of select="$docLangAbstract[1]"/>
      </xsl:when>
      <xsl:when test="string-length(string($charStringAbstract[1])) != 0">
        <xsl:value-of select="string($charStringAbstract[1])"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="string($locStringAbstracts[1])"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
