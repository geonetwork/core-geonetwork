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
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:saxon="http://saxon.sf.net/"
    xmlns:gn="http://www.fao.org/geonetwork"
    xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
    extension-element-prefixes="saxon"
    exclude-result-prefixes="#all">

    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:include href="../../common/base-variables-metadata-editor.xsl"/>

    <xsl:include href="../../common/functions-metadata.xsl"/>

    <xsl:include href="../../common/profiles-loader.xsl"/>

    <xsl:include href="../form-builder.xsl"/>


    <xsl:template match="/">
        <xsl:for-each
            select="/root/*[name(.)!='gui' and name(.)!='request']//*[@gn:addedObj = 'true']">
            <!-- Dispatch to profile mode -->
            <xsl:variable name="profileTemplate" select="concat('dispatch-', $schema)"/>
            <saxon:call-template name="{$profileTemplate}">
                <xsl:with-param name="base" select="."/>
            </saxon:call-template>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
