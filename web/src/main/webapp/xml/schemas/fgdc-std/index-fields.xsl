<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- This file defines what parts of the metadata are indexed by Lucene
		Searches can be conducted on indexes defined here. 
		The Field@name attribute defines the name of the search variable.
		If a variable has to be maintained in the user session, it needs to be 
		added to the GeoNetwork constants in the Java source code.
		Please keep indexes consistent among metadata standards if they should
		work accross different metadata resources -->
	<!-- ========================================================================================= -->

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	
	<!-- ========================================================================================= -->

	<xsl:template match="/">
		<Document>
	
			<xsl:apply-templates select="/metadata/idinfo/citation/citeinfo/title"/>
			
			<xsl:apply-templates select="/metadata/idinfo/descript/abstract"/>
			
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
			</xsl:apply-templates>
			<xsl:apply-templates select="/metadata/idinfo/keywords/place/placekey">
				<xsl:with-param name="name" select="'keyword'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>
			<xsl:apply-templates select="/metadata/idinfo/keywords/stratum/stratkey">
				<xsl:with-param name="name" select="'keyword'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>
			<xsl:apply-templates select="/metadata/idinfo/keywords/temporal/tempkey">
				<xsl:with-param name="name" select="'keyword'"/>
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>
			
			<Field name="any" store="false" index="true">
				<xsl:attribute name="string">
					<xsl:value-of select="normalize-space(string(/metadata))"/>
					<xsl:text> </xsl:text>
					<xsl:for-each select="//*/@*">
						<xsl:value-of select="concat(., ' ')"/>
					</xsl:for-each>
				</xsl:attribute>
			</Field>
			
			<!-- locally searchable fields -->
			
			<!-- digital data format defaults to true. Even if that doesn't make a lot of sense -->
			<Field name="digital" string="true" store="false" index="true" token="false"/>
				
            <!-- not tokenized title for sorting -->
            <Field name="_title" string="{string(/metadata/idinfo/citation/citeinfo/title)}" 
                    store="true" index="true"/>
        </Document>
	</xsl:template>
	
	<!-- ========================================================================================= -->
	
	<!-- text element, by default indexed, not stored, tokenized -->
	<xsl:template match="*">
		<xsl:param name="name"  select="name(.)"/>
		<xsl:param name="store" select="'false'"/>
		<xsl:param name="index" select="'true'"/>
		
	   <Field name="{$name}" string="{string(.)}" store="{$store}" index="{$index}"/>
	</xsl:template>
	
	<!-- ========================================================================================= -->
	
	<!-- latlon coordinates indexed as numeric -->
	<xsl:template match="*" mode="latLon">
		<xsl:param name="name" select="name(.)"/>
		<Field name="{$name}" string="{string(.)}" store="true" index="true" token="false"/>
	</xsl:template>
	
</xsl:stylesheet>
