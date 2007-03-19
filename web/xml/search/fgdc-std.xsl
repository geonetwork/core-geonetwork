<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">
	<Document>

		<xsl:apply-templates select="/metadata/idinfo/citation/citeinfo/title">
			<xsl:with-param name="token" select="'true'"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates select="/metadata/idinfo/descript/abstract">
			<xsl:with-param name="token" select="'true'"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates select="/metadata/idinfo/spdom/bounding/westbc" mode="latLon">
			<xsl:with-param name="name" select="'westBL'"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="/metadata/idinfo/spdom/bounding/eastbc" mode="latLon">
			<xsl:with-param name="name" select="'eastBL'"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="/metadata/idinfo/spdom/bounding/southbc" mode="latLon">
			<xsl:with-param name="name" select="'southBL'"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="/metadata/idinfo/spdom/bounding/northbc" mode="latLon">
			<xsl:with-param name="name" select="'northBL'"/>
		</xsl:apply-templates>
		
		<xsl:apply-templates select="/metadata/idinfo/keywords/theme/themekey">
			<xsl:with-param name="name" select="'keyword'"/>
			<xsl:with-param name="store" select="'true'"/>
			<xsl:with-param name="token" select="'false'"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="/metadata/idinfo/keywords/place/placekey">
			<xsl:with-param name="name" select="'keyword'"/>
			<xsl:with-param name="store" select="'true'"/>
			<xsl:with-param name="token" select="'false'"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="/metadata/idinfo/keywords/stratum/stratkey">
			<xsl:with-param name="name" select="'keyword'"/>
			<xsl:with-param name="store" select="'true'"/>
			<xsl:with-param name="token" select="'false'"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="/metadata/idinfo/keywords/temporal/tempkey">
			<xsl:with-param name="name" select="'keyword'"/>
			<xsl:with-param name="store" select="'true'"/>
			<xsl:with-param name="token" select="'false'"/>
		</xsl:apply-templates>

		<Field name="any" store="false" index="true" token="true">
			<xsl:attribute name="string">
				<xsl:apply-templates select="/metadata" mode="allText"/>
			</xsl:attribute>
		</Field>

		<!-- locally searchable fields -->
		
		<!-- defaults to true -->
		<Field name="digital" string="true" store="false" index="true" token="false"/>
			
	</Document>
</xsl:template>

<!-- text element, by default indexed, not stored, tokenized -->
<xsl:template match="*">
	<xsl:param name="name"  select="name(.)"/>
	<xsl:param name="store" select="'false'"/>
	<xsl:param name="index" select="'true'"/>
	<xsl:param name="token" select="'false'"/>
	
   <Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}" token="{$token}"/>
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
