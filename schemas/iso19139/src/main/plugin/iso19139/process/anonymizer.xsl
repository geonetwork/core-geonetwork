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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:geonet="http://www.fao.org/geonetwork" exclude-result-prefixes="#all"
                version="2.0">

  <!--
    Usage:
      anonymizer?protocol=MYLOCALNETWORK:FILEPATH&email=gis@organisation.org&thesaurus=MYORGONLYTHEASURUS
      * will remove gmd:onLine element with a protocol which starts with MYLOCALNETWORK:FILEPATH.
      * will replace all email ending with @organisation.org by gis@organisation.org
      * will remove all gmd:descriptiveKeywords having MYORGONLYTHEASURUS in their thesaurus name.
  -->

  <!-- Protocol name for which online resource must be removed -->
  <xsl:param name="protocol"/>
  <!-- Generic email to use for all email in same domain (ie. after @domain.org). -->
  <xsl:param name="email"/>
  <!-- Portion of thesaurus name for which keyword should be removed -->
  <xsl:param name="thesaurus"/>


  <xsl:variable name="emailDomain" select="substring-after($email, '@')"/>

  <!-- Remove individual name -->
  <xsl:template match="gmd:individualName" priority="2"/>

  <!-- Remove organisation email by general email -->
  <xsl:template
    match="gmd:electronicMailAddress[$emailDomain != '' and ends-with(gco:CharacterString, $emailDomain)]"
    priority="2">
    <xsl:copy>
      <gco:CharacterString>
        <xsl:value-of select="$email"/>
      </gco:CharacterString>
    </xsl:copy>
  </xsl:template>

  <!-- Remove all resources contact which are not pointOfContact -->
  <xsl:template
    match="gmd:identificationInfo/*/gmd:pointOfContact[gmd:CI_ResponsibleParty/gmd:role/gmd:CI_RoleCode/@codeListValue!='pointOfContact']"
    priority="2"/>

  <!-- Remove all online resource with custom protocol -->
  <xsl:template
    match="gmd:onLine[$protocol != '' and starts-with(gmd:CI_OnlineResource/gmd:protocol/gco:CharacterString, $protocol)]"
    priority="2"/>

  <!-- Remove all descriptive keyword with a thesaurus from $thesaurus -->
  <xsl:template
    match="gmd:descriptiveKeywords[$thesaurus != '' and contains(gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString, $thesaurus)]"
    priority="2"/>

  <!-- Do a copy of every nodes and attributes -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Always remove geonet:* elements. -->
  <xsl:template match="geonet:*" priority="2"/>

</xsl:stylesheet>
