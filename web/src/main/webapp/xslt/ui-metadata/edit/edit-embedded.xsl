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
  Edit metadata embedded processing to add
  a piece of metadata to the editor form
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:saxon="http://saxon.sf.net/"
                xmlns:gn="http://www.fao.org/geonetwork"
                version="2.0"
                extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">

  <xsl:output method="html" encoding="UTF-8" indent="yes"/>

  <xsl:include href="../../common/base-variables-metadata-editor.xsl"/>

  <xsl:include href="../../common/functions-metadata.xsl"/>

  <xsl:include href="../../common/profiles-loader.xsl"/>

  <xsl:include href="../form-builder.xsl"/>


  <xsl:template match="/">

    <xsl:variable name="tempSnippet">
      <!-- Process the added object using schema layout ... -->
      <xsl:for-each
        select="/root/*[name(.)!='gui' and name(.)!='request']//*[@gn:addedObj = 'true']">
        <!-- Dispatch to profile mode -->
        <xsl:variable name="profileTemplate" select="concat('dispatch-', $schema)"/>
        <saxon:call-template name="{$profileTemplate}">
          <xsl:with-param name="base" select="."/>
        </saxon:call-template>
      </xsl:for-each>
    </xsl:variable>

    <xsl:variable name="snippet">
      <xsl:choose>
        <xsl:when test="string($tempSnippet)"><xsl:copy-of select="$tempSnippet" /></xsl:when>
        <xsl:otherwise>
          <!-- If no template defined for the added object, process the parent of the added element using schema layout ... -->
          <xsl:for-each
            select="/root/*[name(.)!='gui' and name(.)!='request']//*[@gn:addedObj = 'true']">
            <!-- Dispatch to profile mode -->
            <xsl:variable name="profileTemplate" select="concat('dispatch-', $schema)"/>
            <saxon:call-template name="{$profileTemplate}">
              <xsl:with-param name="base" select=".."/>
            </saxon:call-template>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- In case the form generated contains multiple children
    element, group them in a container to avoid creation of invalid XML
    which will trigger NPE. This may happen if the container element is skipped
    eg. gmd:extent not defined as a fieldset element, then all its children will
    be on the root. -->
    <xsl:choose>
      <xsl:when test="count($snippet/*) > 1">
        <div>
          <xsl:copy-of select="$snippet/*"/>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$snippet/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
