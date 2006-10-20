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
			<xsl:apply-templates select="mdFileID"/>
			<xsl:apply-templates select="mdLang"/>
			<xsl:apply-templates select="mdChar"/>
			<xsl:apply-templates select="mdParentID"/>
			<xsl:apply-templates select="mdHrLv"/>
			<xsl:apply-templates select="mdHrLvName"/>
			<xsl:apply-templates select="mdContact"/>
			<xsl:apply-templates select="mdDateSt"/>
			<xsl:apply-templates select="mdStanName"/>
			<xsl:apply-templates select="mdStanVer"/>
			<xsl:apply-templates select="distInfo"/>
		 		
			<xsl:choose>
				<xsl:when test="not(dataIdInfo)">
		 			<dataIdInfo>
						<xsl:call-template name="fill"/>
					</dataIdInfo>
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:apply-templates select="dataIdInfo"/>
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:apply-templates select="appSchInfo"/>
			<xsl:apply-templates select="porCatInfo"/>
			<xsl:apply-templates select="mdMaint"/>
			<xsl:apply-templates select="mdConst"/>
			<xsl:apply-templates select="dqInfo"/>
			<xsl:apply-templates select="spatRepInfo"/>
			<xsl:apply-templates select="refSysInfo"/>
			<xsl:apply-templates select="contInfo"/>
			<xsl:apply-templates select="mdExtInfo"/>
		</xsl:copy>
	</xsl:template>

	<!-- ================================================================= -->
	
	<xsl:template match="dataIdInfo">
		<xsl:copy>
			<xsl:apply-templates select="idCitation"/>
			<xsl:apply-templates select="idAbs"/>
			<xsl:apply-templates select="idPurp"/>
			<xsl:apply-templates select="idCredit"/>
			<xsl:apply-templates select="status"/>
			<xsl:apply-templates select="idPoC"/>
			<xsl:apply-templates select="resConst"/>
			<xsl:apply-templates select="dsFormat"/>
			<xsl:apply-templates select="idSpecUse"/>
			<xsl:apply-templates select="resMaint"/>
			<xsl:apply-templates select="descKeys"/>
			<xsl:apply-templates select="graphOver[bgFileDesc != /root/env/type]"/>
		 	
			<xsl:call-template name="fill"/>
		
			<xsl:apply-templates select="spatRpType"/>
			<xsl:apply-templates select="dataScale"/>
			<xsl:apply-templates select="dataLang"/>
			<xsl:apply-templates select="dataChar"/>
			<xsl:apply-templates select="tpCat"/>
			<xsl:apply-templates select="geoBox"/>
			<xsl:apply-templates select="geoDesc"/>
			<xsl:apply-templates select="envirDesc"/>
			<xsl:apply-templates select="dataExt"/>
			<xsl:apply-templates select="suppInfo"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- ================================================================= -->
	
	<xsl:template name="fill">
		<graphOver>
			<bgFileName><xsl:value-of select="/root/env/file"/></bgFileName>
			<bgFileDesc><xsl:value-of select="/root/env/type"/></bgFileDesc>
			<bgFileType><xsl:value-of select="/root/env/ext"/></bgFileType>							
		</graphOver>
	</xsl:template>
	
	<!-- ================================================================= -->

</xsl:stylesheet>
