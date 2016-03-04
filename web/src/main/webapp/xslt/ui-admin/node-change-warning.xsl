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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:geonet="http://www.fao.org/geonetwork">

    <xsl:import href="../base-layout.xsl"/>

    <xsl:variable name="oldNode" select="/root/request/oldNodeId"/>
    <xsl:variable name="lang" select="/root/gui/language"/>
    <xsl:variable name="baseUrl" select="/root/gui/url"/>
    <xsl:variable name="oldNodeHomeUrl"><xsl:value-of select="$baseUrl"/>/<xsl:value-of
            select="$oldNode"/>/<xsl:value-of select="$lang"/>/home</xsl:variable>
    <xsl:variable name="redirectedFrom" select="/root/request/redirectedFrom"/>


    <xsl:template mode="content" match="/">
        <h1>
            <xsl:value-of select="$i18n/nodeChangeWarning"/>
        </h1>
        <div class="alert alert-danger" data-ng-controller="GnLoginController">
            <p>
                <strong><xsl:value-of select="$i18n/nodeChangeInfo"/></strong>
            </p>

            <p>
                <xsl:value-of select="$i18n/nodeChangeBack"/>
                <a href="{$oldNodeHomeUrl}" class="btn btn-link">
                    <xsl:value-of select="$oldNodeHomeUrl"/>
                </a>
            </p>
            <p>

                <xsl:value-of select="$i18n/nodeChangeForward"/>
                <a href="" data-ng-click="nodeChangeRedirect('{$redirectedFrom}')" class="btn btn-link">
                    <xsl:value-of select="$redirectedFrom"/>
                </a>
            </p>
        </div>
    </xsl:template>

</xsl:stylesheet>
