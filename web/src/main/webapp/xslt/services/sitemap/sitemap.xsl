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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:util="java:org.fao.geonet.util.XslUtil"
				version="2.0"
                exclude-result-prefixes="#all">

  <xsl:include href="../../common/base-variables.xsl"/>

  <xsl:variable name="format" select="/root/request/format"/>
  <xsl:variable name="indexDocs" select="/root/response/indexDocs"/>
  <xsl:variable name="changeDate" select="/root/response/changeDate"/>

  <xsl:variable name="sitemapLinkUrl"
                select="util:getSettingValue('system/server/sitemapLinkUrl')"/>


  <xsl:template match="/root">
    <xsl:choose>
      <!-- Return index document -->
      <xsl:when test="string($indexDocs)">
        <xsl:call-template name="indexDoc"/>
      </xsl:when>
      <!-- Return results -->
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="$format='rdf'">
            <xsl:call-template name="rdf"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="xml"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="indexDoc">
    <sitemapindex
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
      xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 https://www.sitemaps.org/schemas/sitemap/0.9/siteindex.xsd">

      <xsl:call-template name="displayIndexDocs">
        <xsl:with-param name="pStart" select="1"/>
        <xsl:with-param name="pEnd" select="$indexDocs"/>
      </xsl:call-template>
    </sitemapindex>
  </xsl:template>


  <xsl:template name="displayIndexDocs">
    <xsl:param name="pStart"/>
    <xsl:param name="pEnd"/>

    <xsl:if test="not($pStart > $pEnd)">
      <xsl:choose>
        <xsl:when test="$pStart = $pEnd">
          <sitemap xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
            <xsl:variable name="formatParam"
                          select="if (string($format)) then concat('format=', $format, '&amp;') else ''"/>
            <loc>
              <xsl:value-of select="concat($nodeUrl, 'api/sitemap?', $formatParam, 'doc=', $pStart)"/>
            </loc>
            <lastmod>
              <xsl:value-of select="substring($changeDate,1,10)"/>
            </lastmod>
          </sitemap>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="vMid" select=
            "floor(($pStart + $pEnd) div 2)"/>
          <xsl:call-template name="displayIndexDocs">
            <xsl:with-param name="pStart" select="$pStart"/>
            <xsl:with-param name="pEnd" select="$vMid"/>
          </xsl:call-template>
          <xsl:call-template name="displayIndexDocs">
            <xsl:with-param name="pStart" select="$vMid+1"/>
            <xsl:with-param name="pEnd" select="$pEnd"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>


  <xsl:template name="xml">
    <urlset
      xmlns:dct="http://purl.org/dc/terms/"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
      xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 https://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
      <xsl:for-each select="metadata/record">
        <xsl:variable name="uuid" select="uuid"/>
        <xsl:variable name="schemaid" select="schemaid"/>
        <xsl:variable name="changedate" select="changedate"/>

        <url>
          <loc>
            <xsl:choose>
              <xsl:when test="$format='xml'">
                <xsl:value-of select="concat($nodeUrl, 'api/records/', $uuid, '/formatters/xml')"/>
              </xsl:when>

              <xsl:otherwise>
                <xsl:variable name="metadataUrl">
                  <xsl:choose>
                    <xsl:when test="contains(upper-case(normalize-space($sitemapLinkUrl)), '{{UUID}}')"><xsl:value-of select="replace($sitemapLinkUrl, '\{\{UUID\}\}', $uuid, 'i' )"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="concat($nodeUrl, 'api/records/', $uuid, '?language=all')"/></xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                <xsl:value-of select="$metadataUrl"/>
              </xsl:otherwise>
            </xsl:choose>
          </loc>
          <lastmod>
            <xsl:value-of select="substring($changedate,1,10)"/>
          </lastmod>

          <!--
          <dct:format>
              <xsl:value-of select="$schemaid"/>
          </dct:format>
          -->

        </url>
      </xsl:for-each>
    </urlset>
  </xsl:template>

  <xsl:template name="rdf">
    <urlset xmlns:sc="http://sw.deri.org/2007/07/sitemapextension/scschema.xsd"
            xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      <sc:dataset>
        <sc:datasetLabel>
          <xsl:value-of select="$env/system/site/name"/> content catalogue for Linked Data spiders
          (RDF)
        </sc:datasetLabel>
        <xsl:for-each select="metadata/record">
          <sc:dataDumpLocation><xsl:value-of select="concat($nodeUrl, 'eng/rdf.metadata.get?uuid=', uuid)"/>
          </sc:dataDumpLocation>
        </xsl:for-each>
        <!--For 5 latests update:
        <sc:sampleURI>http://<server_host>:<server_port>/<catalogue>/metadata/<uuid>.rdf</sc:sampleURI>


        Link to a full dump using the search API
        <sc:dataDumpLocation>http://<server_host>:<server_port>/<catalogue>/search/rdf/</sc:dataDumpLocation>
        or provide for all catalogue record a link using
        <sc:dataDumpLocation>http://<server_host>:<server_port>/<catalogue>/metadata/<uuid>.rdf</sc:dataDumpLocation>-->
        <changefreq>daily</changefreq>
      </sc:dataset>
    </urlset>
  </xsl:template>

</xsl:stylesheet>
