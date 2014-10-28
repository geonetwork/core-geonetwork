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
		<!-- TODO I don't know what tag the language is in so this needs to be done -->
		<xsl:variable name="langCode" select="string(/Metadata/mdLang/languageCode/@value)"/>
		<Document locale="{$langCode}">

			<!-- locale information -->	
	        <Field name="_locale" string="{$langCode}" store="true" index="true"/>
            <Field name="_docLocale" string="{$langCode}" store="true" index="true"/>
      			
        	<!-- For multilingual docs it is good to have a title in the default locale.  In this type of metadata we don't have one but in the general case we do so we need to add it to all -->
            <Field name="_defaultTitle" string="{string(/metadata/idinfo/citation/citeinfo/title)}" store="true" index="true"/>
		
			<xsl:apply-templates select="/metadata/idinfo/citation/citeinfo/title">
				<xsl:with-param name="store" select="'true'"/>
			</xsl:apply-templates>
			
			<xsl:apply-templates select="/metadata/idinfo/descript/abstract">
				<xsl:with-param name="store" select="'true'"/>
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
			
			<xsl:for-each select="/metadata/idinfo/spdom/bounding">
				<Field name="geoBox" string="{concat(westbc, '|', 
					southbc, '|', 
					eastbc, '|', 
					northbc
					)}" store="true" index="false"/>
			</xsl:for-each>
			
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
			<Field name="digital" string="true" store="false" index="true"/>
				
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
		<Field name="{$name}" string="{string(.)}" store="false" index="true"/>
	</xsl:template>
	
</xsl:stylesheet>
