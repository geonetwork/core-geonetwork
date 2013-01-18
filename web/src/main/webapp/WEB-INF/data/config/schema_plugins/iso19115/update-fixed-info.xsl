<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ================================================================= -->
	
	<xsl:template match="/root">
		 <xsl:apply-templates select="Metadata"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="@*|node()">
		 <xsl:copy>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="Metadata">
		 <xsl:copy>
		 		<xsl:if test="not(mdFileID)">
		 			<mdFileID><xsl:value-of select="/root/env/uuid"/></mdFileID>
				</xsl:if>
			  <xsl:apply-templates select="@*|node()"/>
		 </xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="mdFileID">
		 <xsl:copy><xsl:value-of select="/root/env/uuid"/></xsl:copy>
	</xsl:template>
	
	<!-- ================================================================= -->
	
	<xsl:template match="mdDateSt">
        <xsl:choose>
            <xsl:when test="/root/env/changeDate">
                <xsl:copy>
                    <xsl:value-of select="/root/env/changeDate"/>
                </xsl:copy>
 		    </xsl:when>
 		    <xsl:otherwise>
 		        <xsl:copy-of select="."/>
 		    </xsl:otherwise>
        </xsl:choose>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="CharSetCd">
		 <CharSetCd value="utf8"/>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="mdStanName">
		 <xsl:copy>ISO 19115</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="mdStanVer">
		 <xsl:copy>FDIS</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->

	<!--
	online resources: download
	-->
	<xsl:template match="linkage[parent::onLineSrc and matches(following-sibling::protocol,'^WWW:DOWNLOAD-.*-http--download.*') and following-sibling::orName]">
		<linkage>
			<xsl:choose>
				<xsl:when test="/root/env/config/downloadservice/simple='true'">
					<xsl:value-of select="concat(/root/env/siteURL,'/resources.get?id=',/root/env/id,'&amp;fname=',following-sibling::orName,'&amp;access=private')"/>
				</xsl:when>
				<xsl:when test="/root/env/config/downloadservice/withdisclaimer='true'">
					<xsl:value-of select="concat(/root/env/siteURL,'/file.disclaimer?id=',/root/env/id,'&amp;fname=',following-sibling::orName,'&amp;access=private')"/>
				</xsl:when>
				<xsl:otherwise> <!-- /root/env/config/downloadservice/leave='true' -->
					<xsl:value-of select="linkage"/>
				</xsl:otherwise>
			</xsl:choose>
		</linkage>
	</xsl:template>

</xsl:stylesheet>

