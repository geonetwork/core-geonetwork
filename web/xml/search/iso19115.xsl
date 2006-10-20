<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">
	<Document>

		<xsl:apply-templates select="/Metadata/dataIdInfo/idCitation/resTitle">
			<xsl:with-param name="name"  select="'title'"/>
			<xsl:with-param name="token" select="'true'"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates select="/Metadata/dataIdInfo/idAbs">
			<xsl:with-param name="name"  select="'abstract'"/>
			<xsl:with-param name="token" select="'true'"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/westBL" mode="latLon"/>
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/eastBL" mode="latLon"/>
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/southBL" mode="latLon"/>
		<xsl:apply-templates select="/Metadata/dataIdInfo/geoBox/northBL" mode="latLon"/>
		
		<xsl:apply-templates select="/Metadata/dataIdInfo/descKeys/keyword">
			<xsl:with-param name="store" select="'true'"/>
			<xsl:with-param name="token" select="'true'"/>
		</xsl:apply-templates>

		<Field name="any" store="false" index="true" token="true">
			<xsl:attribute name="string">
				<xsl:apply-templates select="/Metadata" mode="allText"/>
			</xsl:attribute>
		</Field>

		<!-- locally searchable fields -->
		
		<xsl:if test="/Metadata/dataIdInfo/idCitation/presForm[contains(PresFormCd/@value, 'Digital')]">
			<Field name="digital" string="true" store="false" index="true" token="false"/>
		</xsl:if>
		
		<xsl:if test="/Metadata/dataIdInfo/idCitation/presForm[contains(PresFormCd/@value, 'Hardcopy')]">
			<Field name="paper" string="true" store="false" index="true" token="false"/>
		</xsl:if>
		
		<!-- FIXME: not handled anymore
		<xsl:apply-templates select="/Metadata/mdFileID">
			<xsl:with-param name="name"  select="'mdID'"/>
			<xsl:with-param name="store" select="'true'"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates select="/Metadata/distInfo/distTranOps/onLineSrc/protocol"/>
		<xsl:apply-templates select="/Metadata/dataIdInfo/tpCat"/>
		-->
		
	</Document>
</xsl:template>

<!-- text element, by default indexed, not stored nor tokenized -->
<xsl:template match="*">
	<xsl:param name="name"  select="name(.)"/>
	<xsl:param name="store" select="'false'"/>
	<xsl:param name="index" select="'true'"/>
	<xsl:param name="token" select="'false'"/>
	
	<Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}" token="{$token}"/>
</xsl:template>

<!-- codelist element, indexed, not stored nor tokenized -->
<xsl:template match="*[./*/@value]">
	<xsl:param name="name" select="name(.)"/>
	
	<Field name="{$name}" string="{*/@value}" store="false" index="true" token="false"/>
</xsl:template>

<!-- latlon coordinates + 360, zero-padded, indexed, not stored, not tokenized -->
<xsl:template match="*" mode="latLon">
	<xsl:param name="name" select="name(.)"/>
	
	<Field name="{$name}" string="{string(.) + 360}" store="true" index="true" token="false"/>
</xsl:template>

<!--allText -->
<xsl:template match="*" mode="allText">
	<xsl:for-each select="@*"><xsl:value-of select="concat(string(.),' ')"/></xsl:for-each>
	<xsl:choose>
		<xsl:when test="*"><xsl:apply-templates select="*" mode="allText"/></xsl:when>
		<xsl:otherwise><xsl:value-of select="concat(string(.),' ')"/></xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
