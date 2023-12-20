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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:os="http://a9.com/-/spec/opensearch/1.1/"
                exclude-result-prefixes="#all">
  <xsl:template match="/">
    <os:OpenSearchDescription xmlns:inspire_dls="http://inspire.ec.europa.eu/schemas/inspire_dls/1.0"
                           xmlns:os="http://a9.com/-/spec/opensearch/1.1/">
      <xsl:choose>
        <xsl:when test="string(/root/response/fileId)">
          <os:ShortName>
            <xsl:value-of select="/root/response/title"/>
          </os:ShortName>
          <os:Description>
            <xsl:value-of select="/root/response/subtitle"/>
          </os:Description>

          <os:Url type="application/opensearchdescription+xml" rel="self">
            <xsl:attribute name="template"
                           select="concat(/root/gui/url,'/opensearch/', /root/gui/language, '/', /root/response/fileId,'/OpenSearchDescription.xml')"/>
          </os:Url>

          <!--Generic URL template for browser integration-->
          <os:Url type="application/atom+xml" rel="results">
            <xsl:attribute name="template"
                           select="concat(/root/gui/url,'/opensearch/', /root/gui/language, '/', /root/response/fileId,'/describe')"/>
          </os:Url>

          <!--Generic URL template for browser integration-->
          <os:Url type="application/xml" rel="results">
            <xsl:attribute name="template"
                           select="concat(/root/gui/url,'/opensearch/', /root/gui/language, '/', /root/response/fileId,'/search?any={searchTerms?}')"/>
          </os:Url>

          <os:Url type="text/html" rel="results">
            <xsl:attribute name="template"
                           select="concat(/root/gui/url,'/opensearch/', /root/gui/language,'/search?q={searchTerms?}')"/>
          </os:Url>
        </xsl:when>
        <xsl:otherwise>
          <os:ShortName><xsl:value-of select="//site/name"/></os:ShortName>
          <os:LongName><xsl:value-of select="//site/organization"/></os:LongName>
          <os:Description><xsl:value-of select="/root/gui/strings/opensearch"/></os:Description>

          <!--Generic URL template for browser integration-->
          <os:Url type="application/xml" rel="results">
            <xsl:attribute name="template"
                           select="concat(/root/gui/url,'/opensearch/', /root/gui/language, '/search?any={searchTerms?}')"/>
          </os:Url>
        </xsl:otherwise>
      </xsl:choose>


      <os:Url type="application/atom+xml" rel="describedby">
        <xsl:attribute name="template"
                       select="concat(/root/gui/url,'/opensearch/', /root/gui/language, '/describe?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}', '&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}', '&amp;language={language?}')"/>
      </os:Url>


      <os:Url rel="results">
        <xsl:attribute name="type"
                       select="/root/response/datasets/dataset[1]/file[1]/type"/>

        <xsl:attribute name="template"
                       select="concat(/root/gui/url,'/opensearch/', /root/gui/language, '/download?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}', '&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;crs={inspire_dls:crs?}', '&amp;language={language?}')"/>
      </os:Url>

      <!-- Repeat the following for each data set, for each CRS of a dataset query example (regardless of the number of file formats -->
      <xsl:for-each select="/root/response/datasets/dataset">
        <xsl:variable name="codeVal" select="code"/>
        <xsl:variable name="namespaceVal" select="namespace"/>

        <xsl:for-each select="file">
          <os:Query role="example"
                 inspire_dls:spatial_dataset_identifier_namespace="{$namespaceVal}"
                 inspire_dls:spatial_dataset_identifier_code="{$codeVal}" inspire_dls:crs="{crs}"
                 language="{lang}" title="{title}" count="1"/>
        </xsl:for-each>

      </xsl:for-each>

      <xsl:choose>
        <xsl:when test="string(/root/response/fileId)">
          <!-- For each Service Feed the contact -->
          <os:Contact><xsl:value-of select="/root/response/authorName"/></os:Contact>
          <os:Tags><xsl:value-of select="/root/response/keywords"/></os:Tags>
          <os:LongName><xsl:value-of select="/root/response/subtitle"/></os:LongName>

          <!-- per Atom Service feed / Service metadata record combinatie: -->
          <os:Developer><xsl:value-of select="/root/response/authorName"/></os:Developer>
          <!--Languages supported by the service. The first language is the default language-->
          <!-- And for each language of the Service Feed: -->
          <os:Language><xsl:value-of select="/root/response/lang"/></os:Language>
        </xsl:when>
        <xsl:otherwise>
          <os:Tags></os:Tags>
          <os:Contact><xsl:value-of select="//feedback/email"/></os:Contact>
          <os:LongName><xsl:value-of select="//site/organization"/></os:LongName>
          <os:Language><xsl:value-of select="/root/gui/language"/></os:Language>
        </xsl:otherwise>
      </xsl:choose>
    </os:OpenSearchDescription>
  </xsl:template>
</xsl:stylesheet>
