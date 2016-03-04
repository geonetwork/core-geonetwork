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

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gfc="http://www.isotc211.org/2005/gfc"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
    xmlns:gml="http://www.opengis.net/gml"
  	xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<!-- =================================================================-->
	
	<xsl:template match="/root">
		<xsl:apply-templates select="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType"/>
	</xsl:template>

	<!-- =================================================================-->
	
	<xsl:template match="gfc:FC_FeatureCatalogue|gfc:FC_FeatureType[not(parent::node())]">
		 <xsl:copy>
		 	<xsl:attribute name="uuid"><xsl:value-of select="/root/env/uuid"/></xsl:attribute>
		 	<xsl:apply-templates select="@*[name(.) != 'uuid']|node()"/>
		 </xsl:copy>
	</xsl:template>
	
	<!-- =================================================================-->
	
	<xsl:template match="gmx:versionDate|gfc:versionDate">
        <xsl:choose>
            <xsl:when test="/root/env/changeDate">
                <xsl:copy>
                    <gco:DateTime><xsl:value-of select="/root/env/changeDate"/></gco:DateTime>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
	</xsl:template>

	<!-- =================================================================-->
	
	<xsl:template match="gmx:characterSet|gfc:characterSet">
	    <xsl:copy>
            <gmd:MD_CharacterSetCode codeListValue="utf8" codeList="MD_CharacterSetCode" />
        </xsl:copy>
	</xsl:template>

	<!-- =================================================================-->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- =================================================================-->
	
	<!-- Handle xsi:nil attribute for max cardinality -->
	<xsl:template match="gco:UnlimitedInteger" priority="2">
		<xsl:variable name="isNil">
			<xsl:choose>
				<xsl:when test="@isInfinite = 'true' or string(.) = ''">
					<xsl:text>true</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>false</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:if test="not(@isInfinite)">
				<xsl:attribute name="isInfinite">
					<xsl:choose>
						<xsl:when test="string(.) = ''">true</xsl:when>
						<xsl:otherwise>false</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</xsl:if>
			<xsl:attribute name="nil" namespace="http://www.w3.org/2001/XMLSchema-instance">
				<xsl:value-of select="$isNil"/>
			</xsl:attribute>
			<xsl:value-of select="."/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="gmx:FileName[name(..)!='gmd:contactInstructions']">
		<xsl:copy>
			<xsl:attribute name="src">
				<xsl:choose>
					<xsl:when test="/root/env/config/downloadservice/simple='true'">
						<xsl:value-of select="concat(/root/env/siteURL,'/resources.get?uuid=',/root/env/uuid,'&amp;fname=',.,'&amp;access=private')"/>
					</xsl:when>
					<xsl:when test="/root/env/config/downloadservice/withdisclaimer='true'">
						<xsl:value-of select="concat(/root/env/siteURL,'/file.disclaimer?uuid=',/root/env/uuid,'&amp;fname=',.,'&amp;access=private')"/>
					</xsl:when>
					<xsl:otherwise> <!-- /root/env/config/downloadservice/leave='true' -->
						<xsl:value-of select="@src"/> <!-- whatever is in there already -->
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:value-of select="."/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@gml:id">
		<xsl:choose>
			<xsl:when test="normalize-space(.)=''">
				<xsl:attribute name="gml:id">
					<xsl:value-of select="generate-id(.)"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>

