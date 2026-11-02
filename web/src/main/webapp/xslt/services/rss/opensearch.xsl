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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <xsl:import href="../../common/base-variables.xsl"/>

  <xsl:template match="/">
    <OpenSearchDescription xmlns="http://a9.com/-/spec/opensearch/1.1/">
      <ShortName>
        <xsl:value-of select="$env/system/site/name"/>
      </ShortName>
      <LongName>
        <xsl:value-of
          select="concat($env/system/site/name, ' (', $env/system/site/organization, ')')"/>
      </LongName>
      <Description>
        <xsl:value-of select="/root/gui/strings/opensearch"/>
      </Description>
      <Tags>Catalogue Metadata ISO19115 ISO19139 DC</Tags>
      <Contact>
        <xsl:value-of select="//feedback/email"/>
      </Contact>
      <Url type="application/rdf+xml">
        <xsl:attribute name="template">
          <xsl:value-of
            select="concat($fullURLForService,'/csw?')"/>
          <xsl:text>service=CSW&amp;version=2.0.2&amp;request=GetRecords&amp;typeNames=csw:Record&amp;resultType=results&amp;elementSetName=full&amp;outputSchema=http://www.w3.org/ns/dcat%23&amp;constraintLanguage=CQL_TEXT&amp;constraint_language_version=1.1.0&amp;constraint=AnyText%20like%20%27%25{searchTerms}%25%27</xsl:text>
        </xsl:attribute>
      </Url>
      <Url type="text/html">
        <xsl:attribute name="template">
          <xsl:value-of
            select="concat($fullURLForService ,'/catalog.search#/search?')"/>
          <xsl:text>any={searchTerms}&amp;hitsPerPage={count?}&amp;bbox={geo:box?}&amp;geometry={geo:geometry?}&amp;name={geo:locationString?}</xsl:text>
        </xsl:attribute>
      </Url>
      <Url type="application/x-suggestions+json">
        <xsl:attribute name="template">
          <xsl:value-of
            select="concat($fullURLForService ,'/suggest?field=anylight&amp;sortBy=STARTSWITHFIRST&amp;')"/>
          <xsl:text>q={searchTerms}</xsl:text>
        </xsl:attribute>
      </Url>
      <Image height="16" width="16" type="image/x-icon">
        <xsl:value-of select="concat($fullURLForWebapp, '/images/logos/favicon.png')"/>
      </Image>
    </OpenSearchDescription>
  </xsl:template>
</xsl:stylesheet>
