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
This is a utility process to try to improve records regarding INSPIRE TG2.
https://github.com/geonetwork/core-geonetwork/pull/3399
-->
<xsl:stylesheet xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:srv="http://www.isotc211.org/2005/srv"
                xmlns:gmx="http://www.isotc211.org/2005/gmx"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:gml320="http://www.opengis.net/gml"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:output indent="yes"/>

  <xsl:variable name="uuid"
                select="//gmd:fileIdentifier/gco:CharacterString"/>

  <xsl:param name="thesauriDir"
             select="'/data/dev/gn/master/web/src/main/webapp/WEB-INF/data/config/codelist'"/>

  <xsl:variable name="inspire-themes"
                select="document(concat('file:///', $thesauriDir, '/external/thesauri/theme/httpinspireeceuropaeutheme-theme.rdf'))//skos:Concept"/>


  <!-- Remove ref to schema location. It is added when exporting the record. -->
  <xsl:template match="@xsi:schemaLocation"/>


  <!-- Update old codelist URL. -->
  <xsl:template match="@codeList[. = 'http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml']">
    <xsl:attribute name="codeList" select="'http://standards.iso.org/iso/19139/resources/gmxCodelists.xml'"/>
  </xsl:template>


  <!-- Move from GML 320 to 321 -->
  <xsl:template match="gml320:*">
    <xsl:element name="{concat('gml:', local-name())}">
      <xsl:apply-templates select="@*|node()|comment()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@gml320:*">
    <xsl:attribute name="{concat('gml:', local-name())}" select="."/>
  </xsl:template>


  <!-- Language MUST be encoded using a LanguageCode -->
  <xsl:template match="gmd:language[gco:CharacterString]">
    <xsl:copy>
      <gmd:LanguageCode codeList="http://www.loc.gov/standards/iso639-2/"
                        codeListValue="{normalize-space(gco:CharacterString)}"/>
    </xsl:copy>
  </xsl:template>


  <!-- Use Anchor for INSPIRE themes -->
  <xsl:template match="gmd:keyword[../gmd:thesaurusName/*/gmd:title/* = 'GEMET - INSPIRE themes, version 1.0']">
    <xsl:variable name="theme"
                  select="gco:CharacterString"/>
    <xsl:variable name="themeInThesaurus"
                  select="$inspire-themes[skos:prefLabel = $theme]"/>
    
    <xsl:choose>
      <xsl:when test="$theme != '' and
                      $themeInThesaurus">
        <xsl:copy>
          <gmx:Anchor xlink:href="{$themeInThesaurus/@rdf:about}"><xsl:value-of select="$theme"/> </gmx:Anchor>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--
  If the keyword value originates from a controlled vocabulary (thesaurus, ontology), for
example GEMET, the citation of the originating controlled vocabulary shall be provided. This
citation shall include at least the title and a reference date (date of publication, date of last
revision or of creation) of the originating controlled vocabulary.
  -->
  <xsl:template match="gmd:thesaurusName/*/gmd:date/*/gmd:dateType/*/@codeListValue">
    <xsl:attribute name="codeListValue">publication</xsl:attribute>
  </xsl:template>


  <!-- Convert CRS to TG2 encoding
  See D.4 Default Coordinate Reference Systems

  List comes from TG2 + some value used in GeoNetwork
  To be completed depending on the past usage for CRS encoding.
  -->
  <xsl:variable name="crsMap">
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4258" label="EPSG:4258">EPSG:4258</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4258" label="EPSG:4258">ETRS89-GRS80</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4258" label="EPSG:4258">ETRS 89 (EPSG:4258)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4936" label="EPSG:4936">ETRS89-XYZ</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4937" label="EPSG:4937">ETRS89-GRS80h</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4258" label="EPSG:4258">ETRS89 (EPSG:4258)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3034" label="EPSG:3034">ETRS89-LCC</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">ETRS89-LAEA</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">ETRS89-LAEA (EPSG:3035)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">ETRS 89 / LAEA Europe (EPSG:3035)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">ETRS89 / LAEA Europe (EPSG:3035)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3038" label="EPSG:3038">ETRS89-TM26N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3039" label="EPSG:3039">ETRS89-TM27N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3040" label="EPSG:3040">ETRS89-TM28N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3041" label="EPSG:3041">ETRS89-TM29N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3042" label="EPSG:3042">ETRS89-TM30N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3043" label="EPSG:3043">ETRS89-TM31N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3044" label="EPSG:3044">ETRS89-TM32N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3045" label="EPSG:3045">ETRS89-TM33N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3046" label="EPSG:3046">ETRS89-TM34N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3047" label="EPSG:3047">ETRS89-TM35N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3048" label="EPSG:3048">ETRS89-TM36N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3049" label="EPSG:3049">ETRS89-TM37N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3050" label="EPSG:3050">ETRS89-TM38N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3051" label="EPSG:3051">ETRS89-TM39N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/7409" label="EPSG:7409">ETRS89-GRS80-EVRS</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/5730" label="EPSG:5730">EVRS</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/5861" label="EPSG:5861">LAT</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/5715" label="EPSG:5715">MSL</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS 1984</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS1984</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS 84 (EPSG:4326)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS84 (EPSG:4326)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS 84 (EPSG:4326)::EPSG</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS84 (EPSG:4326)::EPSG</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">WGS 84 / UTM zone 28N (EPSG:32628)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">WGS84 / UTM zone 28N (EPSG:32628)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32740" label="EPSG:32740">WGS 84 / UTM zone 40S (EPSG:32740)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32740" label="EPSG:32740">WGS84 / UTM zone 40S (EPSG:32740)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4258" label="EPSG:4258">urn:ogc:def:crs:EPSG:7.1:EPSG:4258</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4258" label="EPSG:4258">urn:ogc:def:crs:EPSG:4258</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">urn:ogc:def:crs:EPSG:7.1:3035</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">urn:ogc:def:crs:EPSG:7.1:EPSG:3035</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">urn:ogc:def:crs:EPSG::3035</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">urn:ogc:def:crs:EPSG:7.1:4326</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">urn:ogc:def:crs:EPSG:7.1:EPSG:4326</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">urn:ogc:def:crs:EPSG::4326</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">urn:ogc:def:crs:EPSG:7.1:EPSG:32628</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">urn:ogc:def:crs:EPSG:7.1:32628</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">urn:ogc:def:crs:EPSG:32628</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32740" label="EPSG:32740">urn:ogc:def:crs:EPSG:7.1:EPSG:32740</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32740" label="EPSG:32740">urn:ogc:def:crs:EPSG:7.1:32740</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32740" label="EPSG:32740">urn:ogc:def:crs:EPSG:32740</crs>

    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">2154</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27563" label="EPSG:27563">27563</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32633" label="EPSG:32633">32633</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">4326</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">epsg:2154</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">EPSG 2154</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">EPSG:2154</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">EPSG 2154 (ALV)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">EPSG:2154 - RGF93 / Lambert-93</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27561" label="EPSG:27561">EPSG:27561</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27562" label="EPSG:27562">EPSG:27562</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27571" label="EPSG:27571">EPSG:27571</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27572" label="EPSG:27572">EPSG:27572</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27582" label="EPSG:27582">EPSG:27582</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">GCS_WGS_1984</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">GCS_WGS84</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3035" label="EPSG:3035">http://www.opengis.net/def/crs/EPSG/0/3035</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">Lambert 93</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3948" label="EPSG:3948">Lambert 93 CC48 - EPSG 3948</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27564" label="EPSG:27564">NTF (Paris) / Lambert Corse (EPSG:27564)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27563" label="EPSG:27563">NTF (Paris) / Lambert Sud France (EPSG:27563)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27572" label="EPSG:27572">NTF (Paris) / Lambert zone II (EPSG:27572)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/27573" label="EPSG:27573">NTF (Paris) / Lambert zone III (EPSG:27573)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3942" label="EPSG:3942">RGF93 / CC42 (EPSG:3942)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3943" label="EPSG:3943">RGF93 / CC43 (EPSG:3943)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3944" label="EPSG:3944">RGF93 / CC44 (EPSG:3944)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3945" label="EPSG:3945">RGF93 / CC45 (EPSG:3945)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3947" label="EPSG:3947">RGF93 / CC47 (EPSG:3947)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">RGF93 (EPSG:2154)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">RGF93 / Lambert-93</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">RGF93 Lambert 93</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">RGF93 / Lambert 93 (EPSG:2154)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">RGF93 / Lambert-93 (EPSG:2154)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/2154" label="EPSG:2154">RGF93 Lambert-93 (EPSG:2154)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4471" label="EPSG:4471">RGM04 / UTM zone 38S (EPSG:4471)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">UTM 28N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32629" label="EPSG:32629">UTM 29N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">UTM WGS 84</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">WGS_1984_UTM_zone_28N</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS84</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS 84</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS 84 (EPSG:4326)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS84 géographiques (2D)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/4326" label="EPSG:4326">WGS84 géographiques (3D)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/3234" label="EPSG:3234">WGS 84 / SCAR IMW SR45-46 (EPSG:3234)</crs>
    <crs code="http://www.opengis.net/def/crs/EPSG/0/32628" label="EPSG:32628">WGS84_UTM 28N</crs>
  </xsl:variable>

  <!-- Coordinate reference system

   Validator error report:
   * The metadata record references none of the expected coordinate reference systems.
   The following reference system code have been referenced:
   'urn:ogc:def:crs:EPSG::4326'. Please refer to table 2 of the data specification
   for the list of expected coordinate reference system codes.

   Replace CharacterString with Anchor.
   Replace old label by EPSG:code only
   -->
  <xsl:template match="gmd:referenceSystemIdentifier/gmd:RS_Identifier[gmd:code/gco:CharacterString = $crsMap/crs/text()]">
    <xsl:variable name="code" select="gmd:code/gco:CharacterString"/>
    <xsl:variable name="newCrs" select="($crsMap/crs[text() = $code])[1]"/>

    <gmd:RS_Identifier>
      <xsl:choose>
        <xsl:when test="$newCrs != ''">
          <gmd:code>
            <gmx:Anchor xlink:href="{$newCrs/@code}"><xsl:value-of select="$newCrs/@label"/></gmx:Anchor>
          </gmd:code>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="gmd:code|gmd:codeSpace"/>
        </xsl:otherwise>
      </xsl:choose>
    </gmd:RS_Identifier>
  </xsl:template>


  <!-- Use Anchor for no limitation. May be language dependant. -->
  <xsl:template match="gmd:resourceConstraints/*/gmd:otherConstraints[normalize-space(lower-case(gco:CharacterString)) = (
                          'no limitation',
                          'no limitations',
                          'no limitations apply',
                          'pas de limitation d''utilisation au service',
                          'aucune restriction d''accès public au service de visualisation.',
                          'aucune contrainte d''accès pour la consultation.'
                          )]">
    <gmd:otherConstraints>
      <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations">No limitations to public access</gmx:Anchor>
    </gmd:otherConstraints>
  </xsl:template>

  <xsl:template match="gmd:resourceConstraints/*/gmd:otherConstraints[normalize-space(lower-case(gco:CharacterString)) = (
                            'aucune condition d''utilisation ne s''applique.',
                            'aucune condition ne s''applique',
                            'aucune condition ne s''applique.',
                            'aucune condition ne s’applique',
                            'no conditions apply',
                            'no conditions apply to access and use')]">
    <gmd:otherConstraints>
      <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/ConditionsApplyingToAccessAndUse/noConditionsApply">No conditions apply to access and use</gmx:Anchor>
      <!--<gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/ConditionsApplyingToAccessAndUse/noConditionsApply">Pas de restriction d'accès public</gmx:Anchor>-->
    </gmd:otherConstraints>
  </xsl:template>

  <xsl:template match="gmd:resourceConstraints/*/gmd:otherConstraints[
                        normalize-space(lower-case(gco:CharacterString)) = (
                            'conditions inconnues', 'conditions unknown')]">
    <gmd:otherConstraints>
      <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/ConditionsApplyingToAccessAndUse/conditionsUnknown">Conditions to access and use unknown</gmx:Anchor>
<!--      <gmx:Anchor xlink:href="http://inspire.ec.europa.eu/metadata-codelist/ConditionsApplyingToAccessAndUse/conditionsUnknown">Conditions inconnues</gmx:Anchor>-->
    </gmd:otherConstraints>
  </xsl:template>


  <!-- Do not use RS_Identifier but MD_Identifier for citation id. -->
  <xsl:template match="gmd:MD_Metadata/gmd:identificationInfo/*/gmd:citation/*/gmd:identifier/gmd:RS_Identifier">
    <gmd:MD_Identifier>
      <xsl:apply-templates select="*"/>
    </gmd:MD_Identifier>
  </xsl:template>


  <!-- Add the 2 inspire decimals ... -->
  <xsl:template match="gmd:EX_GeographicBoundingBox/*/gco:Decimal[matches(., '^(-?[0-9]+|[0-9]+.[0-9]{1})$')]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="format-number(., '#0.00')"/>
    </xsl:copy>
  </xsl:template>


  <!--
  Cleanup boolean values and set nilReason attribute if no value.
  -->
  <xsl:template match="gmd:pass">
    <xsl:variable name="booleanValue"
                  select="if (gco:Boolean = ('1', 'true')) then 'true'
                          else if (gco:Boolean = ('0', 'false')) then 'false' else ''"/>
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="$booleanValue = ''">
          <xsl:attribute name="gco:nilReason" select="'unknown'"/>
        </xsl:when>
        <xsl:otherwise>
          <gco:Boolean><xsl:value-of select="$booleanValue"/></gco:Boolean>
        </xsl:otherwise>
      </xsl:choose>

    </xsl:copy>
  </xsl:template>


  <xsl:template match="gmd:version/@gco:nilReason[. = 'missing']">
    <xsl:attribute name="gco:nilReason" select="'unknown'"/>
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
