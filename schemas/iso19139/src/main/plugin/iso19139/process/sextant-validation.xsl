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

<!--
Stylesheet used to update metadata to improve validation.

Restore a backup
CREATE TABLE metadata20190311 AS (SELECT * FROM metadata);
UPDATE metadata a SET data = (SELECT data FROM metadata20190311 b WHERE a.uuid= b.uuid);
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:util="java:org.fao.geonet.util.XslUtil"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output indent="yes"/>

  <xsl:param name="thesauriDir" select="'/data/dev/gn/sextant/web/src/main/webapp/WEB-INF/data/config/codelist'"/>

  <xsl:variable name="inspire-thesaurus"
                select="document(concat('file:///', replace($thesauriDir, '\\', '/'), '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf'))"/>
  <xsl:variable name="inspire-theme"
                select="$inspire-thesaurus//skos:Concept"/>

  <xsl:variable name="uuid"
                select="//gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/>

  <xsl:variable name="defaultCharacterSet"
                select="'utf8'"/>

  <!-- The default language is also added as gmd:locale
  for multilingual metadata records. -->
  <xsl:variable name="mainLanguage"
                select="//gmd:MD_Metadata/gmd:language/gco:CharacterString/text()|
                        //gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>

  <xsl:variable name="isMultilingual"
                select="count(//gmd:MD_Metadata/gmd:locale[*/gmd:languageCode/*/@codeListValue != $mainLanguage]) > 0"/>

  <!--<xsl:variable name="mainLanguageId"
                select="upper-case(util:twoCharLangCode($mainLanguage))"/>-->

  <xsl:variable name="locales"
                select="//gmd:MD_Metadata/gmd:locale/gmd:PT_Locale"/>

  <xsl:variable name="defaultLanguage"
                select="'eng'"/>


  <xsl:variable name="contactEmailMap">
    <contact org="PANGAEA" email="contact@pangaea.de"/>
    <contact org="SISMER" email="sismer@ifremer.fr"/>
    <contact org="IFREMER" email="contact@ifremer.fr"/>
    <contact org="Ifremer" email="contact@ifremer.fr"/>
    <contact org="IFREMER / CENTRE DE BRETAGNE" email="contact@ifremer.fr"/>
    <contact org="IFREMER / CENTRE MANCHE - MER DU NORD" email="contact@ifremer.fr"/>
    <contact org="IGN" email="contact@ign.fr"/>
    <contact org="SHOM" email="contact@shom.fr"/>
  </xsl:variable>

  <!-- Convert CRS to TG2 encoding
  See D.4 Default Coordinate Reference Systems

  List comes from TG2 + some value used in GeoNetwork
  To be completed depending on the past usage for CRS encoding.
  -->
  <xsl:variable name="crsMap">
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4258" label="EPSG:4258">EPSG:4258</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4258" label="EPSG:4258">ETRS89-GRS80</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4258" label="EPSG:4258">ETRS 89 (EPSG:4258)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4936" label="EPSG:4936">ETRS89-XYZ</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4937" label="EPSG:4937">ETRS89-GRS80h</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4258" label="EPSG:4258">ETRS89 (EPSG:4258)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3034" label="EPSG:3034">ETRS89-LCC</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3035" label="EPSG:3035">ETRS89-LAEA</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3035" label="EPSG:3035">ETRS89-LAEA (EPSG:3035)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3035" label="EPSG:3035">ETRS 89 / LAEA Europe (EPSG:3035)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3035" label="EPSG:3035">ETRS89 / LAEA Europe (EPSG:3035)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3038" label="EPSG:3038">ETRS89-TM26N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3039" label="EPSG:3039">ETRS89-TM27N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3040" label="EPSG:3040">ETRS89-TM28N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3041" label="EPSG:3041">ETRS89-TM29N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3042" label="EPSG:3042">ETRS89-TM30N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3043" label="EPSG:3043">ETRS89-TM31N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3044" label="EPSG:3044">ETRS89-TM32N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3045" label="EPSG:3045">ETRS89-TM33N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3046" label="EPSG:3046">ETRS89-TM34N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3047" label="EPSG:3047">ETRS89-TM35N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3048" label="EPSG:3048">ETRS89-TM36N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3049" label="EPSG:3049">ETRS89-TM37N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3050" label="EPSG:3050">ETRS89-TM38N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3051" label="EPSG:3051">ETRS89-TM39N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:7409" label="EPSG:7409">ETRS89-GRS80-EVRS</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:5730" label="EPSG:5730">EVRS</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:5861" label="EPSG:5861">LAT</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:5715" label="EPSG:5715">MSL</crs>
    <!-- It is not clear what label should be in this case? -->
    <crs code="http://codes.wmo.int/grib2/codeflag/4.2/_0-3-3" label="WMO:ISO">ISA</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">WGS 1984</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">WGS1984</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">WGS 84 (EPSG:4326)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">WGS84 (EPSG:4326)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">WGS 84 (EPSG:4326)::EPSG</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">WGS84 (EPSG:4326)::EPSG</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32628" label="EPSG:32628">WGS 84 / UTM zone 28N (EPSG:32628)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32628" label="EPSG:32628">WGS84 / UTM zone 28N (EPSG:32628)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32740" label="EPSG:32740">WGS 84 / UTM zone 40S (EPSG:32740)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32740" label="EPSG:32740">WGS84 / UTM zone 40S (EPSG:32740)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4258" label="EPSG:4258">urn:ogc:def:crs:EPSG:7.1:EPSG:4258</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4258" label="EPSG:4258">urn:ogc:def:crs:EPSG:4258</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3035" label="EPSG:3035">urn:ogc:def:crs:EPSG:7.1:3035</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3035" label="EPSG:3035">urn:ogc:def:crs:EPSG:7.1:EPSG:3035</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:3035" label="EPSG:3035">urn:ogc:def:crs:EPSG::3035</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">urn:ogc:def:crs:EPSG:7.1:4326</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">urn:ogc:def:crs:EPSG:7.1:EPSG:4326</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:4326" label="EPSG:4326">urn:ogc:def:crs:EPSG::4326</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32628" label="EPSG:32628">urn:ogc:def:crs:EPSG:7.1:EPSG:32628</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32628" label="EPSG:32628">urn:ogc:def:crs:EPSG:7.1:32628</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32628" label="EPSG:32628">urn:ogc:def:crs:EPSG:32628</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32740" label="EPSG:32740">urn:ogc:def:crs:EPSG:7.1:EPSG:32740</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32740" label="EPSG:32740">urn:ogc:def:crs:EPSG:7.1:32740</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/EPSG:32740" label="EPSG:32740">urn:ogc:def:crs:EPSG:32740</crs>
  </xsl:variable>


  <!-- INSPIRE Themes / Fix date of the thesaurus

  Validator error report:
  * The content "2018-05-25" of element <DateOfPublication> does not match the required
   simple type. Value "2018-05-25" contravenes the enumeration facet "2008-06-01" of
   the type of element DateOfPublication at column 66, line 10 (GEMET - INSPIRE themes, version 1.0)
   * The metadata record has keywords which originate from a controlled vocabulary
   'GEMET - INSPIRE themes, version 1.0', but the date or date type is not correct.
   Date should be '2008-06-01' and date type 'publication'. The keywords are:
   Area management/restriction/regulation zones and reporting units.

  See https://github.com/geonetwork/core-geonetwork/issues/2500#issuecomment-427818239
  for details -->
  <xsl:template match="gmd:thesaurusName/*[gmd:title/* = 'GEMET - INSPIRE themes, version 1.0']
                        /gmd:date/*/gmd:date/gco:Date[. = '2018-05-25']/text()">2008-06-01</xsl:template>


  <!-- Remove schema location -->
  <xsl:template match="@xsi:schemaLocation"/>


  <!-- INSPIRE Themes / Add Anchor to thesaurus title / TG2

   Validator error report:
   * Missing or wrong Originating Controlled Vocabulary URI - URI needs to be 'http://www.eionet.europa.eu/gemet/inspire_themes'
   -->
  <xsl:template match="gmd:thesaurusName/*/gmd:title/gco:CharacterString[. = 'GEMET - INSPIRE themes, version 1.0']">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; Theme / Adding Anchor for INSPIRE thesaurus title.</xsl:message>

    <gmx:Anchor xlink:href="http://www.eionet.europa.eu/gemet/inspire_themes">GEMET - INSPIRE themes, version 1.0</gmx:Anchor>
  </xsl:template>




  <!-- INSPIRE Themes / Encode keyword using Anchor
   Validator error report:
   * Missing or wrong Keyword URI - URI needs to be 'http://inspire.ec.europa.eu/theme/am'

   TODO: Multilingual records
  <xsl:template match="gmd:descriptiveKeywords/*[
                          gmd:thesaurusName/*/gmd:title/* = 'GEMET - INSPIRE themes, version 1.0']
                        /gmd:keyword/gco:CharacterString">
    <xsl:variable name="theme"
                  select="."/>
    <xsl:variable name="url"
                  select="$inspire-theme[skos:prefLabel = $theme]/@rdf:about"/>
    <xsl:choose>
      <xsl:when test="$url != ''">
        <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; Theme / Using Anchor for '<xsl:value-of select="$theme"/>'.</xsl:message>
        <gmx:Anchor>
          <xsl:attribute name="xlink:href"><xsl:value-of select="$url"/></xsl:attribute>
          <xsl:value-of select="$theme"/>
        </gmx:Anchor>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CHECK]; Theme '<xsl:value-of select="$theme"/>' not found in INSPIRE thesaurus. Keeping it.</xsl:message>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  -->


  <!-- Coordinate reference system

   Validator error report:
   * The metadata record references none of the expected coordinate reference systems.
   The following reference system code have been referenced:
   'urn:ogc:def:crs:EPSG::4326'. Please refer to table 2 of the data specification
   for the list of expected coordinate reference system codes.

   Replace CharacterString with Anchor.
   Replace old label by EPSG:code only

   TODO: Collect list of values
  <xsl:template match="gmd:referenceSystemIdentifier/gmd:RS_Identifier[gmd:code/gco:CharacterString = $crsMap/crs/text()]">
    <xsl:variable name="code" select="gmd:code/gco:CharacterString"/>
    <xsl:variable name="newCrs" select="$crsMap/crs[text() = $code]"/>

    <gmd:RS_Identifier>
      <xsl:choose>
        <xsl:when test="$newCrs != ''">
          <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; CRS / Using Anchor for '<xsl:value-of select="$code"/>'.</xsl:message>
          <gmd:code>
            <gmx:Anchor xlink:href="{$newCrs/@code}"><xsl:value-of select="$newCrs/@label"/></gmx:Anchor>
          </gmd:code>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CHECK]; CRS '<xsl:value-of select="$code"/>' not found CRS table. Keeping it.</xsl:message>
          <xsl:apply-templates select="gmd:code|gmd:codeSpace"/>
        </xsl:otherwise>
      </xsl:choose>
    </gmd:RS_Identifier>
  </xsl:template>
   -->

  <!--
  Some records have contact without email addresse
  Add email from the mapping provided
   -->
  <xsl:template match="gmd:CI_ResponsibleParty[
          gmd:organisationName/*/text() = $contactEmailMap/contact/@org]
                      /gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/
                        gco:CharacterString[normalize-space(.) = '']"
                priority="99">

    <xsl:variable name="orgName"
                  select="normalize-space(ancestor::gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString/text())"/>
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; Adding email to CI_ResponsibleParty '<xsl:value-of select="$orgName"/>' with <xsl:value-of select="$contactEmailMap/contact[@org = $orgName]/@email"/>.</xsl:message>

   <xsl:copy>
    <xsl:value-of select="$contactEmailMap/contact[@org = $orgName]/@email"/>
   </xsl:copy>
  </xsl:template>

  <!--
  Log a message here - this requires a manual fix.
  -->
  <xsl:template match="gmd:CI_ResponsibleParty[
          gmd:organisationName/*/text() != $contactEmailMap/contact/@org]
                        /gmd:contactInfo/*/gmd:address/*/gmd:electronicMailAddress/
                          gco:CharacterString[normalize-space(.) = '']" priority="2">


    <xsl:variable name="orgName"
                  select="normalize-space(ancestor::gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString/text())"/>

    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[MANUAL FIX]; CI_ResponsibleParty '<xsl:value-of select="$orgName"/>' do not have email address.</xsl:message>
    <xsl:copy-of select="."/>
  </xsl:template>


  <!-- Fix date format causing indexing errors. -->
  <xsl:template match="text()[. = '01/01/2007']">
    <xsl:value-of select="'2007-01-01'"/>
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX INDEX]; Fix invalid date format 01/01/2007.</xsl:message>
  </xsl:template>
  
  <xsl:template match="text()[. = '01/01/2011']">
    <xsl:value-of select="'2011-01-01'"/>
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX INDEX]; Fix invalid date format 01/01/2011.</xsl:message>
  </xsl:template>
  
  <xsl:template match="text()[. = 'janvier 2014']">
    <xsl:value-of select="'2014-01'"/>
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX INDEX]; Fix invalid date format janvier 2014.</xsl:message>
  </xsl:template>
  
  <!--
  Some records have self closing reports
  eg.
   <gmd:report/>
   or eg. e74c9c01-1196-4617-86d6-3aec385927e7
   <gmd:report>
      <gmd:DQ_DomainConsistency/>
   </gmd:report>

  Removing them.

  Validator error report:
  * The gmd:DQ_ConformanceResult has an element gmd:pass that must contain a value of type gco:Boolean.
  This metadata record does not contain such a value.
  -->
  <xsl:template match="gmd:report[count(*) = 0]|gmd:report[gmd:DQ_DomainConsistency/count(*) = 0]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing an empty DQ report.</xsl:message>
  </xsl:template>

  <!--
  Some records have self closing contact
  eg.
  <gmd:contact/>
  -->
  <xsl:template match="gmd:contact[count(*) = 0 and @xlink:href]|gmd:pointOfContact[count(*) = 0 and @xlink:href]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing a contact with an xlink <xsl:value-of select="@xlink:href"/>.</xsl:message>
  </xsl:template>

  <xsl:template match="gmd:contact[count(*) = 0]|gmd:pointOfContact[count(*) = 0]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing an empty contact.</xsl:message>
  </xsl:template>

  <!--
  Some records have self closing gmd:temporalElement or empty ones
  eg.
  <gmd:temporalElement/>
  or
  <gmd:temporalElement>
      <gmd:EX_TemporalExtent>
         <gmd:extent>
            <gml:TimePeriod gml:id="d131313366e261a1052958">
               <gml:beginPosition/>
            </gml:TimePeriod>
         </gmd:extent>
      </gmd:EX_TemporalExtent>
   </gmd:temporalElement>
  -->
  <xsl:template match="gmd:temporalElement[count(*) = 0]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing an empty temporalElement.</xsl:message>
  </xsl:template>
  <xsl:template match="gmd:temporalElement[string-join(.//text(), '') = '']">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing a temporalElement with empty children.</xsl:message>
  </xsl:template>



  <!--
  Some records have empty editionDate
  eg.
      <gmd:editionDate>
          <gco:Date/>
       </gmd:editionDate>
  -->
  <xsl:template match="gmd:editionDate[gco:Date = '']">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; Remove empty edition date.</xsl:message>
  </xsl:template>


  <!--
  Some records have self closing gmd:spatialResolution
  eg.
  <gmd:spatialResolution/>
  -->
  <xsl:template match="gmd:spatialResolution[count(*) = 0]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing an empty spatialResolution.</xsl:message>
  </xsl:template>



  <!--
  Some records have self closing distributionFormats
  eg.
  <gmd:distributionFormat/>

  Removing them.
  -->
  <xsl:template match="gmd:transferOptions[normalize-space(*) = '']">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing an empty transferOptions.</xsl:message>
  </xsl:template>
  <xsl:template match="gmd:distributor[normalize-space(*) = '']">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing an empty distributor.</xsl:message>
  </xsl:template>
  <xsl:template match="gmd:distributionFormat[count(*) = 0]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Removing an empty distributionFormat.</xsl:message>
  </xsl:template>



  <!-- Missing hierarchyLevelName -->
  <xsl:template match="gmd:hierarchyLevel[*/@codeListValue != 'dataset' and count(../hierarchyLevelName) = 0]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; Missing hierarchy level name.</xsl:message>
    <xsl:copy-of select="."/>
    <gmd:hierarchyLevelName>
      <gco:CharacterString><xsl:value-of select="*/@codeListValue"/></gco:CharacterString>
    </gmd:hierarchyLevelName>
  </xsl:template>





  <!-- Some records have empty EX_GeographicDescription
  some have "bounding box" as description

   Removing them.
   -->
  <xsl:template match="gmd:extent/gmd:description[gco:CharacterString = 'bounding box']"/>
  <xsl:template match="gmd:geographicElement/gmd:EX_GeographicDescription[count(*) = 0]"/>
  <xsl:template match="gmd:geographicElement[count(*) = 0]"/>



  <!-- Some records have empty DQ reports

   Removing them.
   -->
  <xsl:template match="gmd:report[*/gmd:result/*/gmd:specification/*/gmd:title/* = '']"/>


  <xsl:template match="gmd:pass[gco:Boolean = '']">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[CLEANING]; Not evaluated use nilReason unknown.</xsl:message>
    <gmd:pass gco:nilReason="unknown"/>
  </xsl:template>

  <!-- Some empty DQ section -->
  <xsl:template match="gmd:dataQualityInfo[count(*) = 0]"/>

  <!-- Some DQ section with only a scope-->
  <xsl:template match="gmd:dataQualityInfo[count(*/*[name() != 'gmd:scope']) = 0]"/>

  <!-- Missing statement tag

  -->
  <xsl:template match="gmd:LI_Lineage[gco:CharacterString]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <gmd:statement>
        <xsl:copy-of select="gco:CharacterString"/>
      </gmd:statement>
      <xsl:copy-of select="gmd:processStep"/>
      <xsl:copy-of select="gmd:source"/>
    </xsl:copy>
  </xsl:template>


  <!--
  Some records have missing format version
  eg.
         <gmd:distributionFormat>
            <gmd:MD_Format>
               <gmd:name>
                  <gco:CharacterString>SQLite</gco:CharacterString>
               </gmd:name>
            </gmd:MD_Format>
         </gmd:distributionFormat>
         -->
  <xsl:template match="gmd:distributionFormat/*[not(gmd:version)]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; Missing format version.</xsl:message>

    <xsl:copy>
      <xsl:apply-templates select="gmd:name"/>

      <gmd:version gco:nilReason="missing">
        <gco:CharacterString/>
      </gmd:version>
    </xsl:copy>
  </xsl:template>






  <!--
  Some records have invalid datetime in date

  The value '2011-05-31T12:00:00' of element 'gco:Date' is not valid. (Element: gco:Date with parent element: gmd:date)
         -->
  <xsl:template match="gco:Date[contains(., 'T')]">
    <xsl:message>Record <xsl:value-of select="$uuid"/> ;[FIX]; Date time encoded in a date field.</xsl:message>
    <gco:DateTime><xsl:value-of select="."/></gco:DateTime>
  </xsl:template>



  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|comment()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>


</xsl:stylesheet>
