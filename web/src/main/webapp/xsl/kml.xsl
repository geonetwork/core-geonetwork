<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:geonet="http://www.fao.org/geonetwork"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:exslt="http://exslt.org/common"
                version="1.0"
                exclude-result-prefixes="geonet exslt">
  <xsl:output method="xml"/>
  <xsl:strip-space elements="*"/>
  <xsl:include href="metadata.xsl"/>
  <xsl:include href="utils.xsl"/>

  <xsl:variable name="siteURL"
                select="concat(/root/gui/env/server/protocol,'://',/root/gui/env/server/host,':',/root/gui/env/server/port)"/>

  <xsl:template match="/root">

    <xsl:comment>Metadata transformed into a KML document</xsl:comment>

    <xsl:variable name="remote" select="/root/response/summary/@type='remote'"/>

    <xsl:variable name="md">
      <xsl:if test="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']">
        <xsl:apply-templates mode="brief"
                             select="gmd:MD_Metadata|*[@gco:isoType='gmd:MD_Metadata']"/>
      </xsl:if>
      <xsl:if test="Metadata">
        <xsl:apply-templates mode="brief" select="Metadata"/>
      </xsl:if>
    </xsl:variable>

    <!--    <briefdata1><xsl:copy-of select="$md"/></briefdata1> -->

    <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>

    <xsl:call-template name="kml_record">
      <xsl:with-param name="metadata" select="$metadata"/>
      <xsl:with-param name="remote" select="$remote"/>
    </xsl:call-template>

  </xsl:template>

  <xsl:template name="kml_record">

    <xsl:param name="metadata"/>
    <xsl:param name="remote"/>

    <kml>
      <xsl:variable name="layer" select="/root/request/layers"/>

      <Folder>
        <open>1</open>
        <name>
          <xsl:value-of select="$metadata/title"/>
        </name>
        <Snippet maxLines="1"></Snippet>
        <description>

          <table width="100%" border="0">
            <tr>
              <!--              <xsl:if test="/root/gui/searchDefaults/output = 'full'"> -->
              <td class="padded" align="left" valign="middle" width="200">

                <!-- metadata thumbnail -->
                <xsl:choose>
                  <!-- small thumbnail -->
                  <xsl:when test="$metadata/image[@type='thumbnail']">
                    <xsl:choose>
                      <xsl:when test="contains($metadata/image[@type='thumbnail'],':')">
                        <img src="{$metadata/image[@type='thumbnail']}"
                             alt="{/root/gui/strings/thumbnail}"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <img src="{$siteURL}{$metadata/image[@type='thumbnail']}"
                             alt="{/root/gui/strings/thumbnail}"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:when>
                  <!-- no thumbnail -->
                  <xsl:otherwise>
                    <img src="{$siteURL}{/root/gui/locUrl}/images/nopreview.gif"
                         alt="{/root/gui/strings/thumbnail}"/>
                  </xsl:otherwise>
                </xsl:choose>
              </td>
              <!--              </xsl:if> -->

              <!--
                    <td align="right" valign="top"> <xsl:variable name="source"
                    select="string($metadata/geonet:info/source)"/> <xsl:choose> <xsl:when
                    test="/root/gui/sources/record[string(siteId)=$source]"> <a
                    href="{/root/gui/sources/record[string(siteId)=$source]/baseURL}"
                    target="_blank"> <img
                    src="{$siteURL}{/root/gui/url}/images/logos/{$source}.png"
                    width="40"/> </a> </xsl:when> <xsl:otherwise> <img
                    src="{$siteURL}{/root/gui/url}/images/logos/{$source}.png"
                    width="40"/> </xsl:otherwise> </xsl:choose> </td>
                -->
            </tr>
          </table>
          <p>
            <strong><xsl:value-of select="/root/gui/strings/abstract"/>:
            </strong>
            <xsl:value-of select="$metadata/abstract"/>
          </p>
          <![CDATA[ [<a target="]]><xsl:value-of select="/root/gui/env/site/name"/><xsl:text>" href="</xsl:text><xsl:value-of
          select="$siteURL"/><xsl:value-of
          select="/root/gui/locService"/><![CDATA[/metadata.show?currTab=simple&id=]]><xsl:value-of
          select="$metadata/geonet:info/id"/><![CDATA[">]]><xsl:value-of
          select="/root/gui/strings/descriptionTab"/><![CDATA[</a>]
          [<a target="]]><xsl:value-of select="/root/gui/env/site/name"/><xsl:text>" href="</xsl:text><xsl:value-of
          select="$siteURL"/><xsl:value-of
          select="/root/gui/locService"/><![CDATA[/main.home">]]><xsl:value-of
          select="/root/gui/strings/searchPage"/><![CDATA[</a>]<br/>]]>
        </description>
        <xsl:apply-templates select="$metadata/geoBox"/>
        <!-- for each WMS service -->
        <xsl:for-each select="$metadata/link">
          <xsl:choose>
            <xsl:when test="contains(@type,'application/vnd.ogc.wms_xml')">

              <xsl:variable name="qm">
                <xsl:choose>
                  <xsl:when test="not(contains(@href,'?'))">
                    <xsl:text>?</xsl:text>
                  </xsl:when>
                </xsl:choose>
              </xsl:variable>

              <GroundOverlay>
                <name>
                  <xsl:value-of select="@title"/>
                </name>
                <Icon>
                  <href>
                    <xsl:value-of select="@href"/><xsl:value-of select="$qm"/>VERSION=1.1.1&#0038;REQUEST=GetMap&#0038;SERVICE=WMS&#0038;SRS=EPSG:4326&#0038;WIDTH=512&#0038;HEIGHT=512&#0038;LAYERS=<xsl:value-of
                    select="@name"/>&#0038;TRANSPARENT=TRUE&#0038;STYLES=&#0038;FORMAT=image/png&#0038;
                  </href>
                  <viewRefreshMode>onStop</viewRefreshMode>
                  <viewRefreshTime>3</viewRefreshTime>
                  <viewBoundScale>1.0</viewBoundScale>
                </Icon>
                <xsl:choose>
                  <xsl:when test="$metadata/geoBox">
                    <xsl:apply-templates mode="latlonbox" select="$metadata/geoBox"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <LatLonBox>
                      <north>90</north>
                      <south>-90</south>
                      <east>180</east>
                      <west>-180</west>
                    </LatLonBox>
                  </xsl:otherwise>
                </xsl:choose>

              </GroundOverlay>
              <Document>
                <name>
                  <xsl:value-of select="/root/gui/strings/legend"/>
                </name>
                <ScreenOverlay>
                  <Icon>
                    <href>
                      <xsl:value-of select="@href"/><xsl:value-of select="$qm"/>VERSION=1.1.1&#0038;REQUEST=GetLegendGraphic&#0038;SERVICE=WMS&#0038;LAYER=<xsl:value-of
                      select="@name"/>&#0038;FORMAT=image/png
                    </href>
                  </Icon>
                  <overlayXY x="0" y="1" xunits="fraction" yunits="fraction"/>
                  <screenXY x="5" y="5" xunits="pixels" yunits="insetPixels"/>
                  <rotationXY x="0" y="0" xunits="fraction" yunits="fraction"/>
                  <size x="0" y="0" xunits="fraction" yunits="fraction"/>
                </ScreenOverlay>
              </Document>
            </xsl:when>
          </xsl:choose>
        </xsl:for-each>
      </Folder>
    </kml>
  </xsl:template>

  <xsl:template name="LatLonBox" mode="latlonbox" match="*">
    <LatLonBox>
      <xsl:choose>
        <xsl:when test="northBL">
          <north>
            <xsl:value-of select="northBL"/>
          </north>
        </xsl:when>
        <xsl:otherwise>
          <north>90</north>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:choose>
        <xsl:when test="southBL">
          <south>
            <xsl:value-of select="southBL"/>
          </south>
        </xsl:when>
        <xsl:otherwise>
          <south>-90</south>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:choose>
        <xsl:when test="eastBL">
          <east>
            <xsl:value-of select="eastBL"/>
          </east>
        </xsl:when>
        <xsl:otherwise>
          <east>180</east>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:choose>
        <xsl:when test="westBL">
          <south>
            <xsl:value-of select="westBL"/>
          </south>
        </xsl:when>
        <xsl:otherwise>
          <west>-180</west>
        </xsl:otherwise>
      </xsl:choose>
    </LatLonBox>
  </xsl:template>

  <xsl:template name="lookat" match="*">
    <LookAt>
      <longitude>
        <xsl:choose>
          <xsl:when test="boolean(westBL>eastBL)">
            <xsl:value-of select="round((eastBL - westBL) div 2 + westBL -180)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="round((eastBL - westBL) div 2 + westBL)"/>
          </xsl:otherwise>
        </xsl:choose>
      </longitude>
      <latitude>
        <xsl:choose>
          <xsl:when test="boolean(southBL>northBL)">
            <xsl:value-of select="round((northBL - southBL) div 2 + southBL -90)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="round((northBL - southBL) div 2 + southBL)"/>
          </xsl:otherwise>
        </xsl:choose>
      </latitude>
      <range>
        <xsl:variable name="vert">
          <xsl:value-of select="translate(string(round(northBL - southBL) * 90000),'-','')"/>
        </xsl:variable>
        <xsl:variable name="hori">
          <xsl:value-of select="translate(string(round(eastBL - westBL) * 90000),'-','')"/>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="boolean($vert > $hori) and boolean(180 > $vert)">
            <xsl:value-of select="$vert"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$hori"/>
          </xsl:otherwise>
        </xsl:choose>
      </range>
      <tilt>0.0</tilt>
      <heading>0.0</heading>
    </LookAt>
  </xsl:template>

</xsl:stylesheet>
