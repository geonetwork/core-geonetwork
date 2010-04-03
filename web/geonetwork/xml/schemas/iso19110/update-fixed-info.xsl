<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:gfc="http://www.isotc211.org/2005/gfc"
    xmlns:gco="http://www.isotc211.org/2005/gco"
    xmlns:gmd="http://www.isotc211.org/2005/gmd"
  	xmlns:gmx="http://www.isotc211.org/2005/gmx"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<!-- =================================================================-->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="gfc:FC_FeatureCatalogue"/>
	</xsl:template>

	<!-- =================================================================-->
	
	<xsl:template match="gfc:FC_FeatureCatalogue">
		 <xsl:copy>
		 	<xsl:attribute name="uuid"><xsl:value-of select="/root/env/uuid"/></xsl:attribute>
		 	<xsl:apply-templates select="@*[name(.) != 'uuid']|node()"/>
		 </xsl:copy>
	</xsl:template>
	
	<!-- =================================================================-->
	
	<xsl:template match="gfc:versionDate">
		 <xsl:copy>
            <gco:DateTime><xsl:value-of select="/root/env/changeDate"/></gco:DateTime>
         </xsl:copy>
	</xsl:template>

	<!-- =================================================================-->
	
	<xsl:template match="gfc:characterSet">
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

	<xsl:template match="gmx:FileName">
		<xsl:copy>
			<xsl:attribute name="src">
				<xsl:value-of select="concat(/root/env/siteURL,'/resources.get?id=',/root/env/id,'&amp;fname=',.,'&amp;access=private')"/>
			</xsl:attribute>
			<xsl:value-of select="."/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>

